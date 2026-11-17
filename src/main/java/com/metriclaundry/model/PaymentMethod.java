package com.metriclaundry.model;

public enum PaymentMethod {
    GCASH("GCash"), CASH("Cash on Delivery"), CARD("Credit / Debit Card");

    private final String label;
    PaymentMethod(String label) { this.label = label; }
    public String label() { return label; }
    @Override public String toString() { return label; }
}
