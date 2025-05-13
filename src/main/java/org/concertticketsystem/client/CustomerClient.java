package org.concertticketsystem.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.concertticketsystem.Config;
import org.concertticketsystem.proto.ConcertServiceGrpc;
import org.concertticketsystem.proto.ReservationRequest;
import org.concertticketsystem.proto.ReservationResponse;

public class CustomerClient {
    private final ConcertServiceGrpc.ConcertServiceBlockingStub stub;

    public CustomerClient(String address) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(address)
                .usePlaintext()
                .build();
        stub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    public void makeReservation(String concertId, String seatType, String customerId, boolean includeAfterParty) {
        ReservationRequest request = ReservationRequest.newBuilder()
                .setConcertId(concertId)
                .setSeatType(seatType)
                .setCustomerId(customerId)
                .setIncludeAfterParty(includeAfterParty)
                .build();

        ReservationResponse response = stub.makeReservation(request);
        System.out.println("Reservation ID: " + response.getReservationId() + ", Message: " + response.getMessage());
    }

    public static void main(String[] args) {
        String address = Config.getInstance().getNodeAddresses()[0];
        CustomerClient client = new CustomerClient(address);
        client.makeReservation("concert1", "Regular", "customer1", true);
    }
}