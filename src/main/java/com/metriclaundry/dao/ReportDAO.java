package com.metriclaundry.dao;

import com.metriclaundry.config.Database;
import com.metriclaundry.model.DashboardStats;
import com.metriclaundry.util.AppException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only aggregate queries for dashboards and reports. Revenue = PAID payments only. */
public class ReportDAO {

    public DashboardStats stats() {
        DashboardStats s = new DashboardStats();
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM orders WHERE status <> 'CANCELLED') AS total_orders, "
                + "(SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER') AS total_customers, "
                + "(SELECT COALESCE(SUM(amount),0) FROM payments WHERE status = 'PAID') AS revenue, "
                + "(SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED') AS completed, "
                + "(SELECT COUNT(*) FROM orders WHERE status = 'PLACED') AS pending, "
                + "(SELECT COUNT(*) FROM orders WHERE status IN ('RECEIVED','WASHING','DRYING')) AS in_process, "
                + "(SELECT COUNT(*) FROM orders WHERE status = 'READY') AS ready, "
                + "(SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED' AND DATE(updated_at) = CURDATE()) AS completed_today, "
                + "(SELECT COUNT(*) FROM payments WHERE status = 'PENDING') AS unpaid";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            s.totalOrders = rs.getInt("total_orders");
            s.totalCustomers = rs.getInt("total_customers");
            s.totalRevenue = rs.getBigDecimal("revenue");
            s.completedOrders = rs.getInt("completed");
            s.pendingOrders = rs.getInt("pending");
            s.inProcessOrders = rs.getInt("in_process");
            s.readyOrders = rs.getInt("ready");
            s.completedToday = rs.getInt("completed_today");
            s.unpaidPayments = rs.getInt("unpaid");
            return s;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /** Paid sales per day for the last N days (days without sales return 0). */
    public Map<LocalDate, BigDecimal> dailySales(int days) {
        LinkedHashMap<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++) map.put(start.plusDays(i), BigDecimal.ZERO);
        String sql = "SELECT DATE(paid_at) d, SUM(amount) total FROM payments "
                   + "WHERE status = 'PAID' AND paid_at >= ? GROUP BY DATE(paid_at)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getDate("d").toLocalDate(), rs.getBigDecimal("total"));
            }
            return map;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /** Rows: service, orders, quantity, sales \u2014 for orders created between from and to (inclusive). */
    public List<Object[]> salesByService(LocalDate from, LocalDate to) {
        String sql = "SELECT s.name, s.unit, COUNT(o.id) cnt, COALESCE(SUM(o.quantity),0) qty, "
                   + "COALESCE(SUM(CASE WHEN p.status='PAID' THEN p.amount ELSE 0 END),0) sales "
                   + "FROM services s "
                   + "LEFT JOIN orders o ON o.service_id = s.id AND o.status <> 'CANCELLED' "
                   + "  AND o.created_at >= ? AND o.created_at < ? "
                   + "LEFT JOIN payments p ON p.order_id = o.id "
                   + "GROUP BY s.id, s.name, s.unit ORDER BY sales DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to.plusDays(1)));
            List<Object[]> rows = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getString("name"), rs.getString("unit"), rs.getInt("cnt"),
                            rs.getBigDecimal("qty"), rs.getBigDecimal("sales")});
                }
            }
            return rows;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }
}
