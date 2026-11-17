package com.metriclaundry.model;

public enum Role {
    ADMIN("Administrator"), STAFF("Staff"), CUSTOMER("Customer");

    private final String label;
    Role(String label) { this.label = label; }
    public String label() { return label; }
    @Override public String toString() { return label; }
}
