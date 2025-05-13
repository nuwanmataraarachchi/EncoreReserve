package org.concertticketsystem.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.concertticketsystem.Config;
import org.concertticketsystem.proto.AddConcertRequest;
import org.concertticketsystem.proto.AddConcertResponse;
import org.concertticketsystem.proto.CancelConcertRequest;
import org.concertticketsystem.proto.CancelConcertResponse;
import org.concertticketsystem.proto.ConcertServiceGrpc;
import org.concertticketsystem.proto.SeatTier;

import java.util.Arrays;

public class OrganizerClient {
    private final ConcertServiceGrpc.ConcertServiceBlockingStub stub;

    public OrganizerClient(String address) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(address)
                .usePlaintext()
                .build();
        stub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    public void addConcert(String concertId, String name, String date, int regularCount, int vipCount, int afterPartyTickets) {
        AddConcertRequest request = AddConcertRequest.newBuilder()
                .setConcertId(concertId)
                .setName(name)
                .setDate(date)
                .addAllSeats(Arrays.asList(
                        SeatTier.newBuilder().setType("Regular").setCount(regularCount).setPrice(50.0).build(),
                        SeatTier.newBuilder().setType("VIP").setCount(vipCount).setPrice(100.0).build()
                ))
                .setAfterPartyTickets(afterPartyTickets)
                .build();

        AddConcertResponse response = stub.addConcert(request);
        System.out.println("Add concert: " + response.getMessage());
    }

    public void cancelConcert(String concertId) {
        CancelConcertRequest request = CancelConcertRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        CancelConcertResponse response = stub.cancelConcert(request);
        System.out.println("Cancel concert: " + response.getMessage());
    }

    public static void main(String[] args) {
        String address = Config.getInstance().getNodeAddresses()[0];
        OrganizerClient client = new OrganizerClient(address);
        client.addConcert("concert1", "Rock Fest", "2025-06-01", 100, 50, 20);
    }
}