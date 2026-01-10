package com.gym.app.service;

import com.gym.app.db.DatabaseManager;
import com.gym.app.model.Payment;
import com.gym.app.util.ErrorLogger;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentService {

    /**
     * Adds a new payment record to the database.
     * @param payment The payment object to add.
     * @return The newly created Payment object with the generated ID, or null on failure.
     */
    public Payment addPayment(Payment payment) {
        String sql = "INSERT INTO payments (member_id, amount, payment_date, period_months) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getMemberId());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setDate(3, Date.valueOf(payment.getPaymentDate()));
            pstmt.setInt(4, payment.getPeriodMonths());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentId(generatedKeys.getInt(1));
                        return payment;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to add new payment for member ID: " + payment.getMemberId());
        }
        return null;
    }

    /**
     * Retrieves all payments for a specific member.
     */
    public List<Payment> getPaymentsByMemberId(int memberId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE member_id = ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("member_id"),
                        rs.getDouble("amount"),
                        rs.getDate("payment_date").toLocalDate(),
                        rs.getInt("period_months")
                    ));
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve payments for member ID: " + memberId);
        }
        return payments;
    }

    // Placeholder for reporting functionality (to be completed in Phase 6)
    /**
     * Retrieves all payments.
     */
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                payments.add(new Payment(
                    rs.getInt("payment_id"),
                    rs.getInt("member_id"),
                    rs.getDouble("amount"),
                    rs.getDate("payment_date").toLocalDate(),
                    rs.getInt("period_months")
                ));
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve all payments.");
        }
        return payments;
    }

    /**
     * Retrieves total revenue for a given date range.
     */
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(amount) FROM payments WHERE payment_date BETWEEN ? AND ?";
        double totalRevenue = 0.0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalRevenue = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to calculate total revenue between " + startDate + " and " + endDate);
        }
        return totalRevenue;
    }
}
