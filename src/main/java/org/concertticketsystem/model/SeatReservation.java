package org.concertticketsystem.model;

public class SeatReservation {
    private String tier;
    private int count;

    public SeatReservation(String tier, int count) {
        this.tier = tier;
        this.count = count;
    }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}