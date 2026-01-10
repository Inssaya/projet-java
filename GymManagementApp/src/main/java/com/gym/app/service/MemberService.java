package com.gym.app.service;

import com.gym.app.db.DatabaseManager;
import com.gym.app.model.Member;
import com.gym.app.util.ErrorLogger;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemberService {

    /**
     * Converts a ResultSet row to a Member object.
     */
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("member_id"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getDate("join_date").toLocalDate(),
            rs.getDate("expiry_date").toLocalDate(),
            rs.getString("qr_code_value"),
            rs.getString("photo_path"),
            rs.getString("notes")
        );
    }

    /**
     * Adds a new member to the database. Automatically generates a unique QR ID.
     * @param member The member object to add (qrCodeValue is ignored and generated).
     * @return The newly created Member object with the generated ID and QR value, or null on failure.
     */
    public Member addMember(Member member) {
        String sql = "INSERT INTO members (full_name, phone, join_date, expiry_date, qr_code_value, photo_path, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        // Check if QR code value is already set, if not generate a new one
        if (member.getQrCodeValue() == null || member.getQrCodeValue().isEmpty()) {
            // Automatically generate a UNIQUE QR ID (UUID)
            String qrCodeValue = UUID.randomUUID().toString();
            member.setQrCodeValue(qrCodeValue);
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getFullName());
            pstmt.setString(2, member.getPhone());
            pstmt.setDate(3, Date.valueOf(member.getJoinDate()));
            pstmt.setDate(4, Date.valueOf(member.getExpiryDate()));
            pstmt.setString(5, member.getQrCodeValue());
            pstmt.setString(6, member.getPhotoPath());
            pstmt.setString(7, member.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setMemberId(generatedKeys.getInt(1));
                        return member;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to add new member: " + member.getFullName());
        }
        return null;
    }

    /**
     * Updates an existing member's details.
     */
    public boolean updateMember(Member member) {
        String sql = "UPDATE members SET full_name = ?, phone = ?, join_date = ?, expiry_date = ?, photo_path = ?, notes = ? WHERE member_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getFullName());
            pstmt.setString(2, member.getPhone());
            pstmt.setDate(3, Date.valueOf(member.getJoinDate()));
            pstmt.setDate(4, Date.valueOf(member.getExpiryDate()));
            pstmt.setString(5, member.getPhotoPath());
            pstmt.setString(6, member.getNotes());
            pstmt.setInt(7, member.getMemberId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to update member: " + member.getFullName());
            return false;
        }
    }

    /**
     * Retrieves a member by their ID.
     */
    public Member getMemberById(int memberId) {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve member with ID: " + memberId);
        }
        return null;
    }

    /**
     * Retrieves a member by their QR Code Value. Essential for check-in.
     */
    public Member getMemberByQrCodeValue(String qrCodeValue) {
        String sql = "SELECT * FROM members WHERE qr_code_value = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, qrCodeValue);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve member with QR Code: " + qrCodeValue);
        }
        return null;
    }

    /**
     * Searches members by name, phone, or member ID.
     */
    public List<Member> searchMembers(String searchTerm) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE full_name LIKE ? OR phone LIKE ? OR member_id LIKE ?";
        String searchPattern = "%" + searchTerm + "%";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            // Check if search term is a valid integer for member_id search
            try {
                Integer.parseInt(searchTerm);
                pstmt.setString(3, searchTerm); // Search by exact ID if it's a number
            } catch (NumberFormatException e) {
                pstmt.setString(3, searchPattern); // Fallback to LIKE search
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to search members with term: " + searchTerm);
        }
        return members;
    }

    /**
     * Renews a member's subscription by updating the expiry date.
     * This is part of the Payment & Subscription Workflow.
     * The actual payment record insertion is handled by PaymentService.
     *
     * @param memberId The ID of the member to renew.
     * @param months The number of months to extend the subscription.
     * @return The new expiry date, or null on failure.
     */
    public LocalDate renewSubscription(int memberId, int months) {
        Member member = getMemberById(memberId);
        if (member == null) {
            ErrorLogger.log(new Exception("Member not found"), "Attempted to renew subscription for non-existent member ID: " + memberId);
            return null;
        }

        LocalDate currentExpiry = member.getExpiryDate();
        // If the current expiry is in the past, start the renewal from today.
        // Otherwise, extend from the current expiry date.
        LocalDate baseDate = currentExpiry.isBefore(LocalDate.now()) ? LocalDate.now() : currentExpiry;
        LocalDate newExpiry = baseDate.plusMonths(months);

        String sql = "UPDATE members SET expiry_date = ? WHERE member_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(newExpiry));
            pstmt.setInt(2, memberId);

            if (pstmt.executeUpdate() > 0) {
                return newExpiry;
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to renew subscription for member ID: " + memberId);
        }
        return null;
    }
}
