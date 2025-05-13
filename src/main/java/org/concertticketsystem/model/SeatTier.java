package org.concertticketsystem.model;

public class SeatTier {
    private final String type;
    private int count;
    private final double price;

    public SeatTier(String type, int count, double price) {
        this.type = type;
        this.count = count;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public double getPrice() {
        return price;
    }

    public void setCount(int count) {
        this.count = count;
    }
}