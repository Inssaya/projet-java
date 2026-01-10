package com.gym.app.model;

import java.time.LocalDateTime;

public class Attendance {
    private int attendanceId;
    private int memberId;
    private LocalDateTime timestamp;

    // Constructor for creating a new attendance record (ID will be auto-generated)
    public Attendance(int memberId, LocalDateTime timestamp) {
        this.memberId = memberId;
        this.timestamp = timestamp;
    }

    // Constructor for retrieving an existing attendance record
    public Attendance(int attendanceId, int memberId, LocalDateTime timestamp) {
        this.attendanceId = attendanceId;
        this.memberId = memberId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
