package com.metriclaundry.model;

public enum PaymentStatus {
    UNPAID("Unpaid"), PARTIAL("Partially paid"), PENDING("Pending verification"), PAID("Paid"), VOID("Voided");

    private final String label;
    PaymentStatus(String label) { this.label = label; }
    public String label() { return label; }
    @Override public String toString() { return label; }
}
