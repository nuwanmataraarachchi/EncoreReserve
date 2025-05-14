package org.concertticketsystem.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.concertticketsystem.proto.*;
import java.util.Scanner;

public class OrganizerClient {
    private final ConcertServiceGrpc.ConcertServiceBlockingStub blockingStub;

    public OrganizerClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    public void addConcert(String name, String date, int vipSeats, int generalSeats, int afterPartyTickets) {
        SeatTier vip = SeatTier.newBuilder().setType("VIP").setTotalSeats(vipSeats).setPrice(100.0).build();
        SeatTier general = SeatTier.newBuilder().setType("General").setTotalSeats(generalSeats).setPrice(50.0).build();
        AddConcertRequest request = AddConcertRequest.newBuilder()
                .setName(name)
                .setDate(date)
                .addSeatTiers(vip)
                .addSeatTiers(general)
                .setAfterPartyTickets(afterPartyTickets)
                .build();
        AddConcertResponse response = blockingStub.addConcert(request);
        System.out.println("Add Concert Response: " + response.getMessage() + ", ID: " + response.getConcertId());
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
        OrganizerClient client = new OrganizerClient("localhost", 50051);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nOrganizer CLI");
            System.out.println("1. Add Concert");
            System.out.println("2. List Concerts");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter concert name: ");
                String name = scanner.nextLine();
                System.out.print("Enter date (YYYY-MM-DD): ");
                String date = scanner.nextLine();
                System.out.print("Enter VIP seats: ");
                int vipSeats = scanner.nextInt();
                System.out.print("Enter General seats: ");
                int generalSeats = scanner.nextInt();
                System.out.print("Enter After Party tickets: ");
                int afterPartyTickets = scanner.nextInt();
                client.addConcert(name, date, vipSeats, generalSeats, afterPartyTickets);
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