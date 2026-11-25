package com.metriclaundry.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place for JDBC connections.
 * Settings are read from db.properties (classpath), and can be overridden by a
 * db.properties file next to the jar, or by system properties -Ddb.url=... etc.
 */
public final class Database {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (Exception ignored) { }
//     properties for database
        Path external = Path.of("db.properties");
        
// for this conditions if this was connected on Database
        
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                PROPS.load(in);
            } catch (Exception ignored) { }
        }
        for (String key : new String[]{"db.url", "db.user", "db.password"}) {
            String v = System.getProperty(key);
            if (v != null) PROPS.setProperty(key, v);
        }
    }

    private Database() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url", "jdbc:mysql://localhost:3306/metric_laundry"),
                PROPS.getProperty("db.user", "root"),
                PROPS.getProperty("db.password", ""));
    }

    /** Quick health check used at start-up so the user gets a clear message. */
    public static void testConnection() throws SQLException {
        try (Connection c = getConnection()) {
            if (!c.isValid(3)) throw new SQLException("Database connection is not valid.");
            try (var indexes = c.getMetaData().getIndexInfo(c.getCatalog(), null, "payments", true, false)) {
                while (indexes.next()) {
                    if ("order_id".equalsIgnoreCase(indexes.getString("COLUMN_NAME")))
                        throw new SQLException("Installment payments require database/migrate_partial_payments.sql. Back up the database and run this migration once.");
                }
            }
            // A successful connection alone cannot detect an older incompatible schema.
            for (String query : new String[]{
                    "SELECT id, username, password_hash, salt, role, active FROM users WHERE 1=0",
                    "SELECT id, name, price, unit FROM services WHERE 1=0",
                    "SELECT id, order_no, service_id, status, total FROM orders WHERE 1=0",
                    "SELECT id, receipt_no, method, status FROM payments WHERE 1=0",
                    "SELECT id, status, remarks FROM order_status_history WHERE 1=0"}) {
                try (var statement = c.prepareStatement(query); var rows = statement.executeQuery()) {
                    // Validate the schema without modifying any data.
                } catch (SQLException e) {
                    throw new SQLException("The database schema does not match this desktop app. "
                            + "For the old email-login schema, back up the database and run "
                            + "database/migrate_legacy.sql once. For a new database, import "
                            + "database/metric_laundry.sql (this resets existing data).", e);
                }
            }
        }
    }
}
