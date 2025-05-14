package org.concertticketsystem.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.concertticketsystem.proto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConcertServiceImpl extends ConcertServiceGrpc.ConcertServiceImplBase {
    private static final Logger logger = LogManager.getLogger(ConcertServiceImpl.class);
    private final ZooKeeper zooKeeper;
    private final String concertsPath = "/concerts";

    public ConcertServiceImpl(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void addConcert(AddConcertRequest request, StreamObserver<AddConcertResponse> responseObserver) {
        try {
            String concertId = UUID.randomUUID().toString();
            String path = concertsPath + "/" + concertId;
            Concert concert = Concert.newBuilder()
                    .setConcertId(concertId)
                    .setName(request.getName())
                    .setDate(request.getDate())
                    .addAllSeatTiers(request.getSeatTiersList())
                    .setAfterPartyTickets(request.getAfterPartyTickets())
                    .build();
            zooKeeper.create(path, concert.toByteArray(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            logger.info("Added concert: {}", concertId);
            AddConcertResponse response = AddConcertResponse.newBuilder()
                    .setMessage("Concert added successfully")
                    .setConcertId(concertId)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error adding concert", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    // ... other methods (listConcerts, reserveSeats, etc.)
}