package org.concertticketsystem.proto;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.concertticketsystem.Constants;
import org.concertticketsystem.Config;
import org.concertticketsystem.Node;
import org.concertticketsystem.model.Concert;
import org.concertticketsystem.model.Reservation;
import org.concertticketsystem.model.SeatTier;

import java.util.HashMap;
import java.util.Map;

public class ConcertServiceImpl extends ConcertServiceGrpc.ConcertServiceImplBase {
    private static final Logger logger = LogManager.getLogger(ConcertServiceImpl.class);
    private final Node node;
    private final ZooKeeper zooKeeper;

    public ConcertServiceImpl(Node node) {
        this.node = node;
        try {
            this.zooKeeper = new ZooKeeper(Config.getInstance().getZooKeeperConnectString(),
                    Constants.ZK_SESSION_TIMEOUT, event -> {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to ZooKeeper", e);
        }
    }

    @Override
    public void addConcert(AddConcertRequest req, StreamObserver<AddConcertResponse> responseObserver) {
        if (!node.isLeader()) {
            responseObserver.onNext(AddConcertResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Not the leader")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            Map<String, SeatTier> seatTiers = new HashMap<>();
            for (SeatTier protoTier : req.getSeatsList()) {
                seatTiers.put(protoTier.getType(), new SeatTier(
                        protoTier.getType(), protoTier.getCount(), protoTier.getPrice()));
            }
            Concert concert = new Concert(req.getConcertId(), req.getName(), req.getDate(),
                    seatTiers, req.getAfterPartyTickets());

            // Store in ZooKeeper
            String path = Constants.ZK_CONCERTS_PATH + "/" + req.getConcertId();
            zooKeeper.create(path, serializeConcert(concert), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            // Update local state
            node.getConcerts().put(req.getConcertId(), concert);

            responseObserver.onNext(AddConcertResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Concert added")
                    .build());
        } catch (Exception e) {
            logger.error("Error adding concert", e);
            responseObserver.onNext(AddConcertResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void updateConcert(UpdateConcertRequest req, StreamObserver<UpdateConcertResponse> responseObserver) {
        if (!node.isLeader()) {
            responseObserver.onNext(UpdateConcertResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Not the leader")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            Concert concert = node.getConcerts().get(req.getConcertId());
            if (concert == null) {
                responseObserver.onNext(UpdateConcertResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Concert not found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            Map<String, SeatTier> seatTiers = new HashMap<>();
            for (SeatTier protoTier : req.getSeatsList()) {
                seatTiers.put(protoTier.getType(), new SeatTier(
                        protoTier.getType(), protoTier.getCount(), protoTier.getPrice()));
            }
            concert.getSeatTiers().clear();
            concert.getSeatTiers().putAll(seatTiers);
            concert.setAfterPartyTickets(req.getAfterPartyTickets());

            // Update ZooKeeper
            String path = Constants.ZK_CONCERTS_PATH + "/" + req.getConcertId();
            zooKeeper.setData(path, serializeConcert(concert), -1);

            responseObserver.onNext(UpdateConcertResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Concert updated")
                    .build());
        } catch (Exception e) {
            logger.error("Error updating concert", e);
            responseObserver.onNext(UpdateConcertResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void cancelConcert(CancelConcertRequest req, StreamObserver<CancelConcertResponse> responseObserver) {
        if (!node.isLeader()) {
            responseObserver.onNext(CancelConcertResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Not the leader")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            String path = Constants.ZK_CONCERTS_PATH + "/" + req.getConcertId();
            zooKeeper.delete(path, -1);
            node.getConcerts().remove(req.getConcertId());

            responseObserver.onNext(CancelConcertResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Concert cancelled")
                    .build());
        } catch (Exception e) {
            logger.error("Error cancelling concert", e);
            responseObserver.onNext(CancelConcertResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void makeReservation(ReservationRequest req, StreamObserver<ReservationResponse> responseObserver) {
        try {
            // Two-phase commit for atomicity
            String lockPath = Constants.ZK_CONCERTS_PATH + "/" + req.getConcertId() + "/lock";
            zooKeeper.create(lockPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            Concert concert = node.getConcerts().get(req.getConcertId());
            if (concert == null) {
                zooKeeper.delete(lockPath, -1);
                responseObserver.onNext(ReservationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Concert not found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Reserve seat and after-party ticket atomically
            boolean reserved = concert.reserveSeat(req.getSeatType(), req.getIncludeAfterParty());
            if (!reserved) {
                zooKeeper.delete(lockPath, -1);
                responseObserver.onNext(ReservationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Reservation failed: insufficient tickets")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Commit to ZooKeeper
            String concertPath = Constants.ZK_CONCERTS_PATH + "/" + req.getConcertId();
            zooKeeper.setData(concertPath, serializeConcert(concert), -1);

            // Create reservation
            Reservation reservation = new Reservation(req.getConcertId(), req.getSeatType(),
                    req.getCustomerId(), req.getIncludeAfterParty());

            zooKeeper.delete(lockPath, -1);

            responseObserver.onNext(ReservationResponse.newBuilder()
                    .setSuccess(true)
                    .setReservationId(reservation.getId())
                    .setMessage("Reservation successful")
                    .build());
        } catch (Exception e) {
            logger.error("Error making reservation", e);
            responseObserver.onNext(ReservationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void updateTicketStock(UpdateStockRequest req, StreamObserver<UpdateStockResponse> responseObserver) {
        if (!node.isLeader()) {
            responseObserver.onNext(UpdateStockResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Not the leader")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            Concert concert = node.getConcerts().get(req.getConcertId());
            if (concert == null) {
                responseObserver.onNext(UpdateStockResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Concert not found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            Map<String, SeatTier> seatTiers = new HashMap<>();
            for (SeatTier protoTier : req.getSeatsList()) {
                seatTiers.put(protoTier.getType(), new SeatTier(
                        protoTier.getType(), protoTier.getCount(), protoTier.getPrice()));
            }
            concert.getSeatTiers().clear();
            concert.getSeatTiers().putAll(seatTiers);
            concert.setAfterPartyTickets(req.getAfterPartyTickets());

            // Update ZooKeeper
            String path = Constants.ZK_CONCERTS_PATH + "/" + req.getConcertId();
            zooKeeper.setData(path, serializeConcert(concert), -1);

            responseObserver.onNext(UpdateStockResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Stock updated")
                    .build());
        } catch (Exception e) {
            logger.error("Error updating stock", e);
            responseObserver.onNext(UpdateStockResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    private byte[] serializeConcert(Concert concert) {
        // Simplified serialization (in practice, use JSON or Protobuf)
        String data = concert.getId() + "|" + concert.getName() + "|" + concert.getDate() + "|" +
                concert.getAfterPartyTickets();
        return data.getBytes();
    }
}