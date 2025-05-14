package org.concertticketsystem.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.concertticketsystem.proto.*;
import java.util.Scanner;

public class CustomerClient {
    private final ConcertServiceGrpc.ConcertServiceBlockingStub blockingStub;

    public CustomerClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    public void reserveSeats(String concertId, int vipSeats, int generalSeats, boolean includeAfterParty, int afterPartyCount) {
        SeatReservation vip = SeatReservation.newBuilder().setSeatType("VIP").setCount(vipSeats).build();
        SeatReservation general = SeatReservation.newBuilder().setSeatType("General").setCount(generalSeats).build();
        ReservationRequest request = ReservationRequest.newBuilder()
                .setConcertId(concertId)
                .addSeats(vip)
                .addSeats(general)
                .setIncludeAfterParty(includeAfterParty)
                .setAfterPartyCount(afterPartyCount)
                .build();
        ReservationResponse response = blockingStub.reserveSeats(request);
        System.out.println("Reservation Response: " + response.getMessage() + ", Reservation ID: " + response.getReservationId());
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
        CustomerClient client = new CustomerClient("localhost", 50051);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nCustomer CLI");
            System.out.println("1. Reserve Seats");
            System.out.println("2. List Concerts");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter concert ID: ");
                String concertId = scanner.nextLine();
                System.out.print("Enter VIP seats: ");
                int vipSeats = scanner.nextInt();
                System.out.print("Enter General seats: ");
                int generalSeats = scanner.nextInt();
                System.out.print("Include After Party (true/false): ");
                boolean includeAfterParty = scanner.nextBoolean();
                System.out.print("Enter After Party tickets: ");
                int afterPartyCount = scanner.nextInt();
                client.reserveSeats(concertId, vipSeats, generalSeats, includeAfterParty, afterPartyCount);
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