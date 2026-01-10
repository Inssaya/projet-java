package com.gym.app.model;

import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private int memberId;
    private double amount;
    private LocalDate paymentDate;
    private int periodMonths;

    // Constructor for creating a new payment (ID will be auto-generated)
    public Payment(int memberId, double amount, LocalDate paymentDate, int periodMonths) {
        this.memberId = memberId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.periodMonths = periodMonths;
    }

    // Constructor for retrieving an existing payment
    public Payment(int paymentId, int memberId, double amount, LocalDate paymentDate, int periodMonths) {
        this.paymentId = paymentId;
        this.memberId = memberId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.periodMonths = periodMonths;
    }

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public int getPeriodMonths() { return periodMonths; }
    public void setPeriodMonths(int periodMonths) { this.periodMonths = periodMonths; }
}
