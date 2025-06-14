package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Offer {
    @DocumentId
    private String offerId;
    private String itemId;
    private String sellerId;
    private String buyerId;
    private String buyerDisplayName;
    @Nullable
    private String buyerProfilePictureUrl;
    private double offeredPrice;
    @Nullable
    private String message;
    private String status; // "pending", "accepted", "rejected", "countered", "cancelled_by_buyer", "expired"
    @Nullable
    private Double counterOfferPrice;
    @Nullable
    private String counterOfferMessage;
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;
    @ServerTimestamp
    @Nullable
    private Timestamp updatedAt;
    @Nullable
    private Timestamp expiresAt;

    // Constructor rỗng cần thiết cho Firestore
    public Offer() {
        this.offerId = "";
        this.itemId = "";
        this.sellerId = "";
        this.buyerId = "";
        this.buyerDisplayName = "";
        this.buyerProfilePictureUrl = null;
        this.offeredPrice = 0.0;
        this.message = null;
        this.status = "pending";
        this.counterOfferPrice = null;
        this.counterOfferMessage = null;
        this.createdAt = null;
        this.updatedAt = null;
        this.expiresAt = null;
    }

    // Getters and Setters
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getBuyerDisplayName() { return buyerDisplayName; }
    public void setBuyerDisplayName(String buyerDisplayName) { this.buyerDisplayName = buyerDisplayName; }

    @Nullable
    public String getBuyerProfilePictureUrl() { return buyerProfilePictureUrl; }
    public void setBuyerProfilePictureUrl(@Nullable String buyerProfilePictureUrl) { this.buyerProfilePictureUrl = buyerProfilePictureUrl; }

    public double getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(double offeredPrice) { this.offeredPrice = offeredPrice; }

    @Nullable
    public String getMessage() { return message; }
    public void setMessage(@Nullable String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Nullable
    public Double getCounterOfferPrice() { return counterOfferPrice; }
    public void setCounterOfferPrice(@Nullable Double counterOfferPrice) { this.counterOfferPrice = counterOfferPrice; }

    @Nullable
    public String getCounterOfferMessage() { return counterOfferMessage; }
    public void setCounterOfferMessage(@Nullable String counterOfferMessage) { this.counterOfferMessage = counterOfferMessage; }

    @Nullable
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }

    @Nullable
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@Nullable Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Nullable
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(@Nullable Timestamp expiresAt) { this.expiresAt = expiresAt; }
}