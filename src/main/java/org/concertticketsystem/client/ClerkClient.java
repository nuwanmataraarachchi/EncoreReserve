package org.concertticketsystem.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.concertticketsystem.Config;
import org.concertticketsystem.proto.ConcertServiceGrpc;
import org.concertticketsystem.proto.SeatTier;
import org.concertticketsystem.proto.UpdateStockRequest;
import org.concertticketsystem.proto.UpdateStockResponse;

import java.util.Arrays;

public class ClerkClient {
    private final ConcertServiceGrpc.ConcertServiceBlockingStub stub;

    public ClerkClient(String address) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(address)
                .usePlaintext()
                .build();
        stub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    public void updateTicketStock(String concertId, int regularCount, int vipCount, int afterPartyTickets) {
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setConcertId(concertId)
                .addAllSeats(Arrays.asList(
                        SeatTier.newBuilder().setType("Regular").setCount(regularCount).setPrice(50.0).build(),
                        SeatTier.newBuilder().setType("VIP").setCount(vipCount).setPrice(100.0).build()
                ))
                .setAfterPartyTickets(afterPartyTickets)
                .build();

        UpdateStockResponse response = stub.updateTicketStock(request);
        System.out.println("Update stock: " + response.getMessage());
    }

    public static void main(String[] args) {
        String address = Config.getInstance().getNodeAddresses()[0];
        ClerkClient client = new ClerkClient(address);
        client.updateTicketStock("concert1", 100, 50, 20);
    }
}