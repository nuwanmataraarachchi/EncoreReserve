package org.concertticketsystem.model;

public class SeatTier {
    private String tier;
    private int totalSeats;
    private int availableSeats;
    private double price;

    public SeatTier(String tier, int totalSeats, int availableSeats, double price) {
        this.tier = tier;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public int getCount() { return availableSeats; }
    public void setCount(int count) { this.availableSeats = count; } // Added

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}