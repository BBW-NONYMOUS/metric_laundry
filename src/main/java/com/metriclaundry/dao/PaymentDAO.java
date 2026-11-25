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
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.order_id = ? ORDER BY p.id DESC LIMIT 1")) {
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

//        this add for new update data
        sql.append(" ORDER BY p.created_at DESC, p.id DESC");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            List<Payment> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public List<Payment> history(int orderId) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(
                SELECT + "WHERE p.order_id=? ORDER BY p.created_at, p.id")) {
            ps.setInt(1, orderId);
            List<Payment> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    // All balance-changing operations lock the order first to serialize concurrent payments.
    private BigDecimal lockOrder(Connection c, int orderId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT total,status FROM orders WHERE id=? FOR UPDATE")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new AppException("Order not found.");
                if ("CANCELLED".equals(rs.getString("status"))) throw new AppException("Order is cancelled.");
                return rs.getBigDecimal("total");
            }
        }
    }

    private BigDecimal committed(Connection c, int orderId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE order_id=? AND status IN ('PAID','PENDING')")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        }
    }

    public Payment insert(int orderId, com.metriclaundry.model.PaymentMethod method, BigDecimal amount,
                          String reference, com.metriclaundry.model.User actor) {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal available = lockOrder(c, orderId).subtract(committed(c, orderId));
                if (amount.signum() <= 0 || amount.compareTo(available) > 0)
                    throw new AppException("Amount must be positive and no more than the unreserved balance: " + available
                            + ". Verify or void pending payments first.");
                boolean staff = actor.getRole() != com.metriclaundry.model.Role.CUSTOMER;
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO payments (receipt_no,order_id,method,amount,reference_no,status,received_by,paid_at) VALUES (?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "RC-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0,17));
                    ps.setInt(2, orderId); ps.setString(3, method.name()); ps.setBigDecimal(4, amount);
                    ps.setString(5, reference); ps.setString(6, staff ? "PAID" : "PENDING");
                    if (staff) ps.setInt(7, actor.getId()); else ps.setNull(7, Types.INTEGER);
                    ps.setTimestamp(8, staff ? new Timestamp(System.currentTimeMillis()) : null);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); id = rs.getInt(1); }
                }
                Payment result;
                try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.id=?")) {
                    ps.setInt(1,id);
                    try (ResultSet rs=ps.executeQuery()) { rs.next(); result=map(rs); }
                }
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public void resolve(int paymentId, int actorId, boolean confirm) {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps=c.prepareStatement("SELECT order_id FROM payments WHERE id=?")) {
                    ps.setInt(1,paymentId);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new AppException("Payment not found.");
                        orderId=rs.getInt(1);
                    }
                }
                BigDecimal total=lockOrder(c,orderId);
                if(confirm && committed(c,orderId).compareTo(total)>0)
                    throw new AppException("Payments exceed the order total. Void the incorrect pending entry first.");
                try(PreparedStatement ps=c.prepareStatement(
                        "UPDATE payments SET status=?,received_by=?,paid_at=? WHERE id=? AND status='PENDING'")) {
                    ps.setString(1,confirm ? "PAID" : "VOID"); ps.setInt(2,actorId);
                    ps.setTimestamp(3,confirm ? new Timestamp(System.currentTimeMillis()) : null);
                    ps.setInt(4,paymentId);
                    if(ps.executeUpdate()!=1) throw new AppException("Payment is no longer pending. Refresh the history.");
                }
                c.commit();
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch(SQLException e) { throw new AppException("Database error: " + e.getMessage(),e); }
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
