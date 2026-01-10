package com.gym.app.db;

import com.gym.app.util.ErrorLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_NAME = "gym_management.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Establishes a connection to the SQLite database.
     * @return A Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database by creating all necessary tables if they do not exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Table: members
            String sqlMembers = "CREATE TABLE IF NOT EXISTS members (" +
                                "member_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "full_name TEXT NOT NULL," +
                                "phone TEXT," +
                                "join_date DATE NOT NULL," +
                                "expiry_date DATE NOT NULL," +
                                "qr_code_value TEXT UNIQUE NOT NULL," +
                                "photo_path TEXT," +
                                "notes TEXT" +
                                ");";
            stmt.execute(sqlMembers);

            // Table: payments
            String sqlPayments = "CREATE TABLE IF NOT EXISTS payments (" +
                                 "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 "member_id INTEGER NOT NULL," +
                                 "amount REAL NOT NULL," +
                                 "payment_date DATE NOT NULL," +
                                 "period_months INTEGER NOT NULL," +
                                 "FOREIGN KEY (member_id) REFERENCES members(member_id)" +
                                 ");";
            stmt.execute(sqlPayments);

            // Table: attendance
            String sqlAttendance = "CREATE TABLE IF NOT EXISTS attendance (" +
                                   "attendance_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                   "member_id INTEGER NOT NULL," +
                                   "timestamp DATETIME NOT NULL," +
                                   "FOREIGN KEY (member_id) REFERENCES members(member_id)" +
                                   ");";
            stmt.execute(sqlAttendance);

            // Table: system_info (for license protection)
            String sqlSystemInfo = "CREATE TABLE IF NOT EXISTS system_info (" +
                                   "key TEXT PRIMARY KEY," +
                                   "value TEXT" +
                                   ");";
            stmt.execute(sqlSystemInfo);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to initialize the database schema. Please check if the SQLite JDBC driver is correctly configured.");
        }
    }
}
