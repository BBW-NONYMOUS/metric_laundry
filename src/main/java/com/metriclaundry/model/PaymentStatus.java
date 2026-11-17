package com.metriclaundry.model;

public enum PaymentStatus {
    PENDING("Pending"), PAID("Paid");

    private final String label;
    PaymentStatus(String label) { this.label = label; }
    public String label() { return label; }
    @Override public String toString() { return label; }
}
