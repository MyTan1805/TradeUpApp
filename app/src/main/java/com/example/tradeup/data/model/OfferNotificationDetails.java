package com.example.tradeup.data.model;

public class OfferNotificationDetails {
    private String offerId;
    private double offeredPrice;
    private String status; // "pending", "accepted", "rejected", "countered"

    // Constructor rỗng cần thiết cho Firestore
    public OfferNotificationDetails() {
        this.offerId = "";
        this.offeredPrice = 0.0;
        this.status = "";
    }

    public OfferNotificationDetails(String offerId, double offeredPrice, String status) {
        this.offerId = offerId;
        this.offeredPrice = offeredPrice;
        this.status = status;
    }

    // Getters and Setters
    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public double getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(double offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}