package com.n26.model;

/**
 * Class holding data about Transaction
 */
public class Transaction {

    private final long timestamp;
    private final double amount;

    public Transaction() {
        this.amount = 0.0;
        this.timestamp = 0;
    }

    public Transaction(long timestamp, double amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "timestamp=" + timestamp +
                ", amount=" + amount +
                '}';
    }
}
