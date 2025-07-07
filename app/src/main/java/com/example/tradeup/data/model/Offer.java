// File: src/main/java/com/example/tradeup/data/model/Offer.java
// << PHIÊN BẢN CẬP NHẬT ĐỂ HỖ TRỢ ĐÀM PHÁN >>
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

    // --- CÁC TRƯỜNG ĐƯỢC THAY ĐỔI/THÊM MỚI ---
    private double currentPrice; // Giá hiện tại đang được đàm phán
    @Nullable
    private String lastMessage; // Tin nhắn đi kèm với lần trả giá cuối
    private String lastActionByUid; // UID của người thực hiện hành động cuối (người đưa ra giá `currentPrice`)

    // --- CÁC TRƯỜNG CŨ GIỮ LẠI ---
    private String status; // pending, accepted, rejected, expired
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;
    @ServerTimestamp
    @Nullable
    private Timestamp updatedAt;
    @Nullable
    private Timestamp expiresAt;

    public Offer() {
        // Constructor rỗng cần thiết cho Firestore
    }

    // --- GETTERS VÀ SETTERS ---
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
    @Nullable public String getBuyerProfilePictureUrl() { return buyerProfilePictureUrl; }
    public void setBuyerProfilePictureUrl(@Nullable String buyerProfilePictureUrl) { this.buyerProfilePictureUrl = buyerProfilePictureUrl; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    @Nullable public String getLastMessage() { return lastMessage; }
    public void setLastMessage(@Nullable String lastMessage) { this.lastMessage = lastMessage; }
    public String getLastActionByUid() { return lastActionByUid; }
    public void setLastActionByUid(String lastActionByUid) { this.lastActionByUid = lastActionByUid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Nullable public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }
    @Nullable public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@Nullable Timestamp updatedAt) { this.updatedAt = updatedAt; }
    @Nullable public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(@Nullable Timestamp expiresAt) { this.expiresAt = expiresAt; }
}