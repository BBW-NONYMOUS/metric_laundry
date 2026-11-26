package com.metriclaundry.dao;

import com.metriclaundry.config.Database;
import com.metriclaundry.model.Order;
import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.model.PaymentStatus;
import com.metriclaundry.model.StatusHistory;
import com.metriclaundry.util.AppException;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private static final String SELECT =
            "SELECT o.*, u.full_name AS customer_name, s.name AS service_name, s.unit AS unit, COALESCE(p.paid,0) AS paid_amount, COALESCE(p.pending,0) AS pending_amount "
          + "FROM orders o "
          + "JOIN users u ON u.id = o.customer_id "
          + "JOIN services s ON s.id = o.service_id "
          + "LEFT JOIN (SELECT order_id, SUM(CASE WHEN status='PAID' THEN amount ELSE 0 END) paid, SUM(CASE WHEN status='PENDING' THEN amount ELSE 0 END) pending FROM payments GROUP BY order_id) p ON p.order_id = o.id ";

    /**
     * Inserts the order and its first history row in ONE transaction and
     * generates the order number ML-yyyyMMdd### (e.g. ML-20261109001).
     */
    public Order insert(Order o, int createdBy) {
        String prefix = "ML-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < 5; attempt++) {
            try (Connection c = Database.getConnection()) {
                c.setAutoCommit(false);
                try {
                    o.setAddOns(AddOnDAO.validate(c, o.getAddOns()));
                    o.setTotal(o.getServiceSubtotal().add(o.getAddOnTotal()));
                    int seq;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM orders WHERE order_no LIKE ?")) {
                        ps.setString(1, prefix + "%");
                        try (ResultSet rs = ps.executeQuery()) { rs.next(); seq = rs.getInt(1) + 1 + attempt; }
                    }
                    String orderNo = prefix + String.format("%03d", seq);
                    int id;
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO orders (order_no, customer_id, service_id, quantity, unit_price, total, status, "
                          + "notes, estimated_completion, created_by) VALUES (?,?,?,?,?,?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, orderNo);
                        ps.setInt(2, o.getCustomerId());
                        ps.setInt(3, o.getServiceId());
                        ps.setBigDecimal(4, o.getQuantity());
                        ps.setBigDecimal(5, o.getUnitPrice());
                        ps.setBigDecimal(6, o.getTotal());
                        ps.setString(7, OrderStatus.PLACED.name());
                        ps.setString(8, o.getNotes());
                        ps.setTimestamp(9, Timestamp.valueOf(o.getEstimatedCompletion()));
                        ps.setInt(10, createdBy);
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); id = rs.getInt(1); }
                    }
                    AddOnDAO.insert(c, id, o.getAddOns());
                    insertHistory(c, id, OrderStatus.PLACED, createdBy, "Order placed");
                    c.commit();
                    return findById(id);
                } catch (SQLIntegrityConstraintViolationException dup) {
                    c.rollback();               // order number collision -> retry with next number
                } catch (SQLException | RuntimeException e) {
                    c.rollback();
                    throw e;
                }
            } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
        }
        throw new AppException("Could not generate a unique order number. Please try again.");
    }

    public Order findById(int id) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE o.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) return null;
                Order order = map(rs);
                order.setAddOns(AddOnDAO.history(c, id));
                return order; }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /**
     * Flexible search used by every order table.
     * @param customerId 0 = all customers
     * @param status     null = all statuses
     * @param activeOnly only orders not yet completed / cancelled
     * @param keyword    matches order no, customer or service name
     * @param limit      0 = no limit
     */
    public List<Order> search(int customerId, OrderStatus status, boolean activeOnly, String keyword, int limit) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (customerId > 0) { sql.append(" AND o.customer_id = ?"); params.add(customerId); }
        if (status != null) { sql.append(" AND o.status = ?"); params.add(status.name()); }
        if (activeOnly) sql.append(" AND o.status NOT IN ('COMPLETED','CANCELLED')");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_no LIKE ? OR u.full_name LIKE ? OR s.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        sql.append(" ORDER BY o.created_at DESC, o.id DESC");
        if (limit > 0) sql.append(" LIMIT ").append(limit);
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            List<Order> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /** Changes the status and writes the audit row in one transaction. */
    public void updateStatus(int orderId, OrderStatus newStatus, int changedBy, String remarks) {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
                try (PreparedStatement lock = c.prepareStatement("SELECT total FROM orders WHERE id=? FOR UPDATE")) {
                    lock.setInt(1,orderId);
                    try (ResultSet rs=lock.executeQuery()) {
                        if(!rs.next()) throw new AppException("Order not found.");
                        java.math.BigDecimal total=rs.getBigDecimal(1);
                        try(PreparedStatement sums=c.prepareStatement("SELECT COALESCE(SUM(CASE WHEN status='PAID' THEN amount ELSE 0 END),0), COALESCE(SUM(CASE WHEN status='PENDING' THEN amount ELSE 0 END),0) FROM payments WHERE order_id=?")) {
                            sums.setInt(1,orderId);
                            try(ResultSet amounts=sums.executeQuery()) {
                                amounts.next();
                                if(newStatus==OrderStatus.COMPLETED && amounts.getBigDecimal(1).compareTo(total)<0)
                                    throw new AppException("The full balance must be paid before completing the order.");
                                if(newStatus==OrderStatus.CANCELLED && amounts.getBigDecimal(1).add(amounts.getBigDecimal(2)).signum()>0)
                                    throw new AppException("Cannot cancel an order with confirmed or pending payments. Resolve payments first; refunds require separate handling.");
                            }
                        }
                    }
                }
                ps.setString(1, newStatus.name());
                ps.setInt(2, orderId);
                ps.executeUpdate();
                insertHistory(c, orderId, newStatus, changedBy, remarks);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public List<StatusHistory> history(int orderId) {
        String sql = "SELECT h.status, h.remarks, h.changed_at, u.full_name FROM order_status_history h "
                   + "LEFT JOIN users u ON u.id = h.changed_by WHERE h.order_id = ? ORDER BY h.changed_at, h.id";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            List<StatusHistory> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StatusHistory(OrderStatus.valueOf(rs.getString("status")), rs.getString("full_name"),
                            rs.getString("remarks"), rs.getTimestamp("changed_at").toLocalDateTime()));
                }
            }
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    private void insertHistory(Connection c, int orderId, OrderStatus status, int by, String remarks) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO order_status_history (order_id, status, changed_by, remarks) VALUES (?,?,?,?)")) {
            ps.setInt(1, orderId);
            ps.setString(2, status.name());
            ps.setInt(3, by);
            ps.setString(4, remarks);
            ps.executeUpdate();
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderNo(rs.getString("order_no"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setServiceId(rs.getInt("service_id"));
        o.setServiceName(rs.getString("service_name"));
        o.setUnit(rs.getString("unit"));
        o.setQuantity(rs.getBigDecimal("quantity"));
        o.setUnitPrice(rs.getBigDecimal("unit_price"));
        o.setTotal(rs.getBigDecimal("total"));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setNotes(rs.getString("notes"));
        Timestamp est = rs.getTimestamp("estimated_completion");
        if (est != null) o.setEstimatedCompletion(est.toLocalDateTime());
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        o.setPaidAmount(rs.getBigDecimal("paid_amount"));
        o.setPendingAmount(rs.getBigDecimal("pending_amount"));
        return o;
    }
}
