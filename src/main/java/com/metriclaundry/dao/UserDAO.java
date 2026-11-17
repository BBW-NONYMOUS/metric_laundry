package com.metriclaundry.dao;

import com.metriclaundry.config.Database;
import com.metriclaundry.model.Role;
import com.metriclaundry.model.User;
import com.metriclaundry.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String COLS =
            "id, username, full_name, email, phone, address, role, active, created_at";

    /** Returns {user, hash, salt} or null when the username does not exist. */
    public Object[] findCredentials(String username) {
        String sql = "SELECT " + COLS + ", password_hash, salt FROM users "
                + "WHERE username = ? OR email = ? ORDER BY (username = ?) DESC LIMIT 1";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Object[]{map(rs), rs.getString("password_hash"), rs.getString("salt")};
            }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public User findById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public boolean usernameExists(String username, int excludeId) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE username = ? AND id <> ?")) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    /** Lists users of the given roles; keyword matches name, username, phone or email. */
    public List<User> search(String keyword, Role... roles) {
        StringBuilder sql = new StringBuilder("SELECT " + COLS + " FROM users WHERE role IN (");
        for (int i = 0; i < roles.length; i++) sql.append(i == 0 ? "?" : ",?");
        sql.append(")");
        boolean hasKw = keyword != null && !keyword.isBlank();
        if (hasKw) sql.append(" AND (full_name LIKE ? OR username LIKE ? OR phone LIKE ? OR email LIKE ?)");
        sql.append(" ORDER BY full_name");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            for (Role r : roles) ps.setString(i++, r.name());
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                for (int k = 0; k < 4; k++) ps.setString(i++, like);
            }
            List<User> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
            return list;
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public int insert(User u, String hash, String salt) {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, phone, address, role, active) "
                   + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getPhone());
            ps.setString(7, u.getAddress());
            ps.setString(8, u.getRole().name());
            ps.setBoolean(9, u.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public void update(User u) {
        String sql = "UPDATE users SET username=?, full_name=?, email=?, phone=?, address=?, role=?, active=? WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getAddress());
            ps.setString(6, u.getRole().name());
            ps.setBoolean(7, u.isActive());
            ps.setInt(8, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public void updatePassword(int userId, String hash, String salt) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=?, salt=? WHERE id=?")) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    public int countActiveAdmins() {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='ADMIN' AND active=1");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) { throw new AppException("Database error: " + e.getMessage(), e); }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) u.setCreatedAt(t.toLocalDateTime());
        return u;
    }
}
