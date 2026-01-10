package com.gym.app.service;

import com.gym.app.db.DatabaseManager;
import com.gym.app.model.Attendance;
import com.gym.app.util.ErrorLogger;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceService {

    /**
     * Logs a new attendance record for a member.
     * @param memberId The ID of the member checking in.
     * @return The newly created Attendance object with the generated ID, or null on failure.
     */
    public Attendance logAttendance(int memberId) {
        String sql = "INSERT INTO attendance (member_id, timestamp) VALUES (?, ?)";
        LocalDateTime now = LocalDateTime.now();
        Attendance attendance = new Attendance(memberId, now);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, memberId);
            // SQLite does not have a native DATETIME type, so we store it as a string
            pstmt.setString(2, now.toString());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attendance.setAttendanceId(generatedKeys.getInt(1));
                        return attendance;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to log attendance for member ID: " + memberId);
        }
        return null;
    }

    /**
     * Retrieves all attendance records for a given member.
     */
    public List<Attendance> getAttendanceByMemberId(int memberId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE member_id = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Convert the stored string back to LocalDateTime
                    LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));
                    attendanceList.add(new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("member_id"),
                        timestamp
                    ));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve attendance for member ID: " + memberId);
        }
        return attendanceList;
    }

    // Placeholder for reporting functionality (to be completed in Phase 6)
    /**
     * Retrieves all attendance records.
     */
    public List<Attendance> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance ORDER BY timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Convert the stored string back to LocalDateTime
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));
                attendanceList.add(new Attendance(
                    rs.getInt("attendance_id"),
                    rs.getInt("member_id"),
                    timestamp
                ));
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve all attendance records.");
        }
        return attendanceList;
    }

    /**
     * Retrieves the count of attendance records for a given date range.
     */
    public int getAttendanceCount(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE timestamp BETWEEN ? AND ?";
        int count = 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDateTime.toString());
            pstmt.setString(2, endDateTime.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to calculate attendance count between " + startDateTime + " and " + endDateTime);
        }
        return count;
    }
}
