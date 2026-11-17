package com.metriclaundry.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Display formatting helpers (peso amounts and dates). */
public final class Formats {
    public static final String PESO = "\u20B1";
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy - h:mm a");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");

    private Formats() { }

    public static String peso(BigDecimal v) { return PESO + MONEY.format(v == null ? BigDecimal.ZERO : v); }
    public static String date(LocalDateTime d) { return d == null ? "-" : DATE.format(d); }
    public static String date(LocalDate d) { return d == null ? "-" : DATE.format(d); }
    public static String dateTime(LocalDateTime d) { return d == null ? "-" : DATE_TIME.format(d); }

    /** "Today, 11:30 AM", "Tomorrow, 9:00 AM" or a full date. */
    public static String friendly(LocalDateTime d) {
        if (d == null) return "-";
        LocalDate today = LocalDate.now();
        if (d.toLocalDate().equals(today)) return "Today, " + TIME.format(d);
        if (d.toLocalDate().equals(today.plusDays(1))) return "Tomorrow, " + TIME.format(d);
        return dateTime(d);
    }
}
