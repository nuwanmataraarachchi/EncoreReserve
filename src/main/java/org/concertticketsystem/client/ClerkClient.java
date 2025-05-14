package org.concertticketsystem.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.concertticketsystem.proto.*;
import java.util.Scanner;

public class ClerkClient {
    private final ConcertServiceGrpc.ConcertServiceBlockingStub blockingStub;

    public ClerkClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    public void updateStock(String concertId, int vipSeats, int generalSeats, int afterPartyTickets) {
        SeatTier vip = SeatTier.newBuilder().setType("VIP").setTotalSeats(vipSeats).setPrice(100.0).build();
        SeatTier general = SeatTier.newBuilder().setType("General").setTotalSeats(generalSeats).setPrice(50.0).build();
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setConcertId(concertId)
                .addSeatTiers(vip)
                .addSeatTiers(general)
                .setAfterPartyTickets(afterPartyTickets)
                .build();
        UpdateStockResponse response = blockingStub.updateStock(request);
        System.out.println("Update Stock Response: " + response.getMessage());
    }

    public void listConcerts() {
        ListConcertsRequest request = ListConcertsRequest.newBuilder().build();
        ListConcertsResponse response = blockingStub.listConcerts(request);
        System.out.println("Concerts:");
        for (Concert concert : response.getConcertsList()) {
            System.out.println("ID: " + concert.getConcertId() + ", Name: " + concert.getName() + ", Date: " + concert.getDate());
            for (SeatTier tier : concert.getSeatTiersList()) {
                System.out.println("  Seat Tier: " + tier.getType() + ", Seats: " + tier.getTotalSeats() + ", Price: " + tier.getPrice());
            }
            System.out.println("  After Party Tickets: " + concert.getAfterPartyTickets());
        }
    }

    public static void main(String[] args) {
        ClerkClient client = new ClerkClient("localhost", 50051);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nClerk CLI");
            System.out.println("1. Update Stock");
            System.out.println("2. List Concerts");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter concert ID: ");
                String concertId = scanner.nextLine();
                System.out.print("Enter VIP seats to add: ");
                int vipSeats = scanner.nextInt();
                System.out.print("Enter General seats to add: ");
                int generalSeats = scanner.nextInt();
                System.out.print("Enter After Party tickets to add: ");
                int afterPartyTickets = scanner.nextInt();
                client.updateStock(concertId, vipSeats, generalSeats, afterPartyTickets);
            } else if (choice == 2) {
                client.listConcerts();
            } else if (choice == 3) {
                break;
            } else {
                System.out.println("Invalid option");
            }
        }
        scanner.close();
    }
}