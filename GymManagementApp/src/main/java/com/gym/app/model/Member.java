package com.gym.app.model;

import java.time.LocalDate;

public class Member {
    private int memberId;
    private String fullName;
    private String phone;
    private LocalDate joinDate;
    private LocalDate expiryDate;
    private String qrCodeValue;
    private String photoPath;
    private String notes;

    // Constructor for creating a new member (ID will be auto-generated)
    public Member(String fullName, String phone, LocalDate joinDate, LocalDate expiryDate, String qrCodeValue, String photoPath, String notes) {
        this.fullName = fullName;
        this.phone = phone;
        this.joinDate = joinDate;
        this.expiryDate = expiryDate;
        this.qrCodeValue = qrCodeValue;
        this.photoPath = photoPath;
        this.notes = notes;
    }

    // Constructor for retrieving an existing member
    public Member(int memberId, String fullName, String phone, LocalDate joinDate, LocalDate expiryDate, String qrCodeValue, String photoPath, String notes) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.phone = phone;
        this.joinDate = joinDate;
        this.expiryDate = expiryDate;
        this.qrCodeValue = qrCodeValue;
        this.photoPath = photoPath;
        this.notes = notes;
    }

    // Getters and Setters
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getQrCodeValue() { return qrCodeValue; }
    public void setQrCodeValue(String qrCodeValue) { this.qrCodeValue = qrCodeValue; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Helper method for membership status
    public String getStatus() {
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return "Expired";
        } else if (expiryDate.isBefore(today.plusDays(7))) {
            return "Expiring";
        } else {
            return "Active";
        }
    }
}
