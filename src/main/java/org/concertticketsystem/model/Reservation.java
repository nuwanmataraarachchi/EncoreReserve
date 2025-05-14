package org.concertticketsystem.model;

import java.util.List;

public class Reservation {
    private String id;
    private String concertId;
    private List<SeatReservation> seatReservations;

    public Reservation(String id, String concertId, List<SeatReservation> seatReservations) {
        this.id = id;
        this.concertId = concertId;
        this.seatReservations = seatReservations;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConcertId() { return concertId; }
    public void setConcertId(String concertId) { this.concertId = concertId; }
    public List<SeatReservation> getSeatReservations() { return seatReservations; }
    public void setSeatReservations(List<SeatReservation> seatReservations) { this.seatReservations = seatReservations; }
}