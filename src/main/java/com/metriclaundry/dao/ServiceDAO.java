package com.metriclaundry.dao;

import com.metriclaundry.config.Database;
import com.metriclaundry.model.LaundryService;
import com.metriclaundry.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<LaundryService> findAll(boolean activeOnly) {
        String sql = "SELECT * FROM services" + (activeOnly ? " WHERE active = 1" : "") + " ORDER BY id";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<LaundryService> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public LaundryService findById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM services WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public void save(LaundryService s) {
        boolean insert = s.getId() == 0;
        String sql = insert
                ? "INSERT INTO services (name, price, unit, description, turnaround_hours, active) VALUES (?,?,?,?,?,?)"
                : "UPDATE services SET name=?, price=?, unit=?, description=?, turnaround_hours=?, active=? WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setBigDecimal(2, s.getPrice());
            ps.setString(3, s.getUnit());
            ps.setString(4, s.getDescription());
            ps.setInt(5, s.getTurnaroundHours());
            ps.setBoolean(6, s.isActive());
            if (!insert) ps.setInt(7, s.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("A service named \"" + s.getName() + "\" already exists.");
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /** Deletes the service; if it is already used by orders it is deactivated instead. */
    public boolean deleteOrDeactivate(int id) {
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement chk = c.prepareStatement("SELECT 1 FROM orders WHERE service_id = ? LIMIT 1")) {
                chk.setInt(1, id);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement ps = c.prepareStatement("UPDATE services SET active = 0 WHERE id = ?")) {
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
                        return false;
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM services WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    private LaundryService map(ResultSet rs) throws SQLException {
        LaundryService s = new LaundryService();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setPrice(rs.getBigDecimal("price"));
        s.setUnit(rs.getString("unit"));
        s.setDescription(rs.getString("description"));
        s.setTurnaroundHours(rs.getInt("turnaround_hours"));
        s.setActive(rs.getBoolean("active"));
        return s;
    }
}
