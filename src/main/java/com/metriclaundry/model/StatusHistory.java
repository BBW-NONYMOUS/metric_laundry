package com.metriclaundry.model;

import java.time.LocalDateTime;

/** One row of the order audit trail (used by the tracking timeline). */
public class StatusHistory {
    private OrderStatus status;
    private String changedByName;
    private String remarks;
    private LocalDateTime changedAt;

    public StatusHistory() { }
    public StatusHistory(OrderStatus status, String changedByName, String remarks, LocalDateTime changedAt) {
        this.status = status; this.changedByName = changedByName; this.remarks = remarks; this.changedAt = changedAt;
    }
    public OrderStatus getStatus() { return status; }
    public String getChangedByName() { return changedByName; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
