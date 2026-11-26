package com.metriclaundry.model;
import java.math.BigDecimal;
public record AddOn(int id, String name, String unit, BigDecimal price, boolean active) {
    public boolean available() { return active && price != null && price.signum() > 0; }
}
