package com.metriclaundry.dao;

import com.metriclaundry.config.Database;
import com.metriclaundry.model.Payment;
import com.metriclaundry.model.PaymentMethod;
import com.metriclaundry.model.PaymentStatus;
import com.metriclaundry.util.AppException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    private static final String SELECT =
            "SELECT p.*, o.order_no, c.full_name AS customer_name, r.full_name AS received_by_name "
          + "FROM payments p JOIN orders o ON o.id = p.order_id "
          + "JOIN users c ON c.id = o.customer_id "
          + "LEFT JOIN users r ON r.id = p.received_by ";

    public Payment findByOrder(int orderId) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public List<Payment> search(PaymentStatus status, String keyword) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null) { sql.append(" AND p.status = ?"); params.add(status.name()); }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.receipt_no LIKE ? OR o.order_no LIKE ? OR c.full_name LIKE ? OR p.reference_no LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            for (int i = 0; i < 4; i++) params.add(like);
        }
        sql.append(" ORDER BY p.created_at DESC, p.id DESC");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            List<Payment> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /**
     * Creates the payment for an order, or replaces it while it is still PENDING
     * (e.g. the customer switches from Cash to GCash).
     */
    public void upsert(int orderId, String receiptNo, PaymentMethod method, BigDecimal amount, String refNo,
                       PaymentStatus status, Integer receivedBy) {
        String sql = "INSERT INTO payments (receipt_no, order_id, method, amount, reference_no, status, received_by, paid_at) "
                   + "VALUES (?,?,?,?,?,?,?,?) "
                   + "ON DUPLICATE KEY UPDATE method=VALUES(method), amount=VALUES(amount), reference_no=VALUES(reference_no), "
                   + "status=VALUES(status), received_by=VALUES(received_by), paid_at=VALUES(paid_at)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, receiptNo);
            ps.setInt(2, orderId);
            ps.setString(3, method.name());
            ps.setBigDecimal(4, amount);
            ps.setString(5, refNo);
            ps.setString(6, status.name());
            if (receivedBy == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, receivedBy);
            if (status == PaymentStatus.PAID) ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            else ps.setNull(8, Types.TIMESTAMP);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public void markPaid(int paymentId, int receivedBy) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE payments SET status='PAID', received_by=?, paid_at=NOW() WHERE id=? AND status='PENDING'")) {
            ps.setInt(1, receivedBy);
            ps.setInt(2, paymentId);
            if (ps.executeUpdate() == 0) throw new AppException("Payment is already confirmed or does not exist.");
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setReceiptNo(rs.getString("receipt_no"));
        p.setOrderId(rs.getInt("order_id"));
        p.setOrderNo(rs.getString("order_no"));
        p.setCustomerName(rs.getString("customer_name"));
        p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setReferenceNo(rs.getString("reference_no"));
        p.setStatus(PaymentStatus.valueOf(rs.getString("status")));
        p.setReceivedByName(rs.getString("received_by_name"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) p.setPaidAt(paid.toLocalDateTime());
        return p;
    }
}
