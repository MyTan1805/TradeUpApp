// File: src/main/java/com/example/tradeup/data/model/Transaction.java

package com.example.tradeup.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Transaction implements Parcelable {
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
    private String paymentMethod; // "COD" hoặc "Online"
    private String paymentStatus; // "pending", "completed", "failed"
    private String shippingStatus;
    private boolean ratingGivenByBuyer;
    private boolean ratingGivenBySeller;
    @Nullable
    private String paymentIntentId;
    private String deliveryAddress;

    public Transaction() {}

    // Parcelable constructor
    protected Transaction(Parcel in) {
        transactionId = in.readString();
        itemId = in.readString();
        itemTitle = in.readString();
        itemImageUrl = in.readString();
        sellerId = in.readString();
        buyerId = in.readString();
        priceSold = in.readDouble();
        transactionDate = in.readParcelable(Timestamp.class.getClassLoader());
        paymentMethod = in.readString();
        paymentStatus = in.readString();
        shippingStatus = in.readString(); // *** THÊM DÒNG NÀY ***
        ratingGivenByBuyer = in.readByte() != 0;
        ratingGivenBySeller = in.readByte() != 0;
        paymentIntentId = in.readString(); // *** THÊM DÒNG NÀY ***
        deliveryAddress = in.readString();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionId);
        dest.writeString(itemId);
        dest.writeString(itemTitle);
        dest.writeString(itemImageUrl);
        dest.writeString(sellerId);
        dest.writeString(buyerId);
        dest.writeDouble(priceSold);
        dest.writeParcelable(transactionDate, flags);
        dest.writeString(paymentMethod);
        dest.writeString(paymentStatus);
        dest.writeString(shippingStatus); // *** THÊM DÒNG NÀY ***
        dest.writeByte((byte) (ratingGivenByBuyer ? 1 : 0));
        dest.writeByte((byte) (ratingGivenBySeller ? 1 : 0));
        dest.writeString(paymentIntentId); // *** THÊM DÒNG NÀY ***
        dest.writeString(deliveryAddress);
    }

    // Getters and Setters (giữ nguyên, không cần thay đổi)
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(@Nullable String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Nullable
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(@Nullable String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    @Nullable
    public String getShippingStatus() { return shippingStatus; }
    public void setShippingStatus(@Nullable String shippingStatus) { this.shippingStatus = shippingStatus; }

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

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public boolean isRatingGivenByBuyer() { return ratingGivenByBuyer; }
    public void setRatingGivenByBuyer(boolean ratingGivenByBuyer) { this.ratingGivenByBuyer = ratingGivenByBuyer; }

    public boolean isRatingGivenBySeller() { return ratingGivenBySeller; }
    public void setRatingGivenBySeller(boolean ratingGivenBySeller) { this.ratingGivenBySeller = ratingGivenBySeller; }

    public boolean isCompleted() {
        return "completed".equals(paymentStatus);
    }
}