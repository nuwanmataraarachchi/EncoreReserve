package org.concertticketsystem.model;

import java.util.UUID;

public class Reservation {
    private final String id;
    private final String concertId;
    private final String seatType;
    private final String customerId;
    private final boolean includeAfterParty;

    public Reservation(String concertId, String seatType, String customerId, boolean includeAfterParty) {
        this.id = UUID.randomUUID().toString();
        this.concertId = concertId;
        this.seatType = seatType;
        this.customerId = customerId;
        this.includeAfterParty = includeAfterParty;
    }

    public String getId() {
        return id;
    }

    public String getConcertId() {
        return concertId;
    }

    public String getSeatType() {
        return seatType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public boolean isIncludeAfterParty() {
        return includeAfterParty;
    }
}