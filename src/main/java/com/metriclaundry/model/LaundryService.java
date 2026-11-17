package com.metriclaundry.model;

import java.math.BigDecimal;

/** A service offered by the shop, e.g. Washing P30.00 / kg. */
public class LaundryService {
    private int id;
    private String name;
    private BigDecimal price = BigDecimal.ZERO;
    private String unit = "KG";          // KG or PC
    private String description;
    private int turnaroundHours = 2;
    private boolean active = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTurnaroundHours() { return turnaroundHours; }
    public void setTurnaroundHours(int turnaroundHours) { this.turnaroundHours = turnaroundHours; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** "kg" or "pc" for display. */
    public String unitLabel() { return "PC".equalsIgnoreCase(unit) ? "pc" : "kg"; }

    @Override public String toString() { return name; }
}
