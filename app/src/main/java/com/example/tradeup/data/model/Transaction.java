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
    private boolean ratingGivenByBuyer;
    private boolean ratingGivenBySeller;
    private boolean sellerConfirmed;
    private boolean buyerConfirmed;

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
        ratingGivenByBuyer = in.readByte() != 0;
        ratingGivenBySeller = in.readByte() != 0;
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
        dest.writeByte((byte) (ratingGivenByBuyer ? 1 : 0));
        dest.writeByte((byte) (ratingGivenBySeller ? 1 : 0));
        dest.writeString(deliveryAddress);
    }


    @Override
    public int describeContents() {
        return 0;
    }
    private String deliveryAddress;
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(@Nullable String deliveryAddress) { this.deliveryAddress = deliveryAddress; }


    // Getters and Setters
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

    public boolean isSellerConfirmed() {
        return sellerConfirmed;
    }

    public boolean isBuyerConfirmed() {
        return buyerConfirmed;
    }
}