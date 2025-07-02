// File: src/main/java/com/example/tradeup/data/model/Transaction.java

package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Transaction {
    @DocumentId
    private String transactionId;
    private String itemId;
    private String itemTitle;
    @Nullable
    private String itemImageUrl;
    private String sellerId;
    private String buyerId;
    private double priceSold;
    @ServerTimestamp
    @Nullable
    private Timestamp transactionDate;
    @Nullable
    private String paymentMethod;
    private boolean ratingGivenByBuyer;
    private boolean ratingGivenBySeller;

    // <<< SỬA LỖI: CHỈ CẦN MỘT CONSTRUCTOR RỖNG KHÔNG CÓ THÂN HÀM >>>
    public Transaction() {
        // Để trống. Firestore sẽ tự động xử lý.
    }

    // --- Getters and Setters (Giữ nguyên) ---
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    @Nullable
    public String getItemImageUrl() { return itemImageUrl; }
    public void setItemImageUrl(@Nullable String itemImageUrl) { this.itemImageUrl = itemImageUrl; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public double getPriceSold() { return priceSold; }
    public void setPriceSold(double priceSold) { this.priceSold = priceSold; }

    @Nullable
    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(@Nullable Timestamp transactionDate) { this.transactionDate = transactionDate; }

    @Nullable
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(@Nullable String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isRatingGivenByBuyer() { return ratingGivenByBuyer; }
    public void setRatingGivenByBuyer(boolean ratingGivenByBuyer) { this.ratingGivenByBuyer = ratingGivenByBuyer; }

    public boolean isRatingGivenBySeller() { return ratingGivenBySeller; }
    public void setRatingGivenBySeller(boolean ratingGivenBySeller) { this.ratingGivenBySeller = ratingGivenBySeller; }
}