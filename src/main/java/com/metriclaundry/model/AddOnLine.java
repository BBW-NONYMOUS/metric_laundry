package com.metriclaundry.model;
import java.math.BigDecimal;
import java.math.RoundingMode;
public record AddOnLine(int addonId, String name, String unit, BigDecimal quantity, BigDecimal unitPrice) {
    public BigDecimal total() { return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP); }
    public String description() { return name + " - " + quantity.stripTrailingZeros().toPlainString() + " " + unit + " x " + com.metriclaundry.util.Formats.peso(unitPrice) + " = " + com.metriclaundry.util.Formats.peso(total()); }
}
