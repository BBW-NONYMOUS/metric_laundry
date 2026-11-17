package com.metriclaundry.model;

import java.util.List;

/**
 * Laundry order life-cycle. The normal flow is strictly linear:
 * PLACED -> RECEIVED -> WASHING -> DRYING -> READY -> COMPLETED.
 * CANCELLED is a terminal side-exit.
 */
public enum OrderStatus {
    PLACED("Order Placed"),
    RECEIVED("Received"),
    WASHING("Washing"),
    DRYING("Drying"),
    READY("Ready for Pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;
    OrderStatus(String label) { this.label = label; }

    public String label() { return label; }

    /** The ordered steps shown in the tracker (excludes CANCELLED). */
    public static List<OrderStatus> flow() {
        return List.of(PLACED, RECEIVED, WASHING, DRYING, READY, COMPLETED);
    }

    /** Next step in the flow, or null if there is none. */
    public OrderStatus next() {
        List<OrderStatus> f = flow();
        int i = f.indexOf(this);
        return (i < 0 || i == f.size() - 1) ? null : f.get(i + 1);
    }

    public boolean isActive() { return this != COMPLETED && this != CANCELLED; }

    /** Text used for the coloured status badge in tables. */
    public String badgeText() {
        switch (this) {
            case PLACED: return "Pending";
            case COMPLETED: return "Completed";
            case CANCELLED: return "Cancelled";
            case READY: return "Ready";
            default: return "In Progress";
        }
    }

    @Override public String toString() { return label; }
}
