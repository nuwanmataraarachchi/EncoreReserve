package org.concertticketsystem.model;

import java.util.HashMap;
import java.util.Map;

public class Concert {
    private final String id;
    private final String name;
    private final String date;
    private final Map<String, SeatTier> seatTiers;
    private int afterPartyTickets;

    public Concert(String id, String name, String date, Map<String, SeatTier> seatTiers, int afterPartyTickets) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.seatTiers = new HashMap<>(seatTiers);
        this.afterPartyTickets = afterPartyTickets;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public Map<String, SeatTier> getSeatTiers() {
        return new HashMap<>(seatTiers);
    }

    public int getAfterPartyTickets() {
        return afterPartyTickets;
    }

    public void setAfterPartyTickets(int tickets) {
        this.afterPartyTickets = tickets;
    }

    public synchronized boolean reserveSeat(String seatType, boolean includeAfterParty) {
        SeatTier tier = seatTiers.get(seatType);
        if (tier == null || tier.getCount() <= 0) {
            return false;
        }
        if (includeAfterParty && afterPartyTickets <= 0) {
            return false;
        }
        tier.setCount(tier.getCount() - 1);
        if (includeAfterParty) {
            afterPartyTickets--;
        }
        return true;
    }
}