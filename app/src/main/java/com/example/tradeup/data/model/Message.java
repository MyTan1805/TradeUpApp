package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Message {
    @DocumentId
    private String messageId;
    private String senderId;
    private String receiverId;
    @Nullable
    private String text;
    @Nullable
    private String imageUrl;
    private String type; // "text", "image", "offer_notification"
    @ServerTimestamp
    @Nullable
    private Timestamp timestamp;
    private boolean isRead;
    @Nullable
    private OfferNotificationDetails offerDetails;

    // Constructor rỗng cần thiết cho Firestore
    public Message() {
        this.messageId = "";
        this.senderId = "";
        this.receiverId = "";
        this.text = null;
        this.imageUrl = null;
        this.type = "text";
        this.timestamp = null;
        this.isRead = false;
        this.offerDetails = null;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    @Nullable
    public String getText() { return text; }
    public void setText(@Nullable String text) { this.text = text; }
    @Nullable
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(@Nullable String imageUrl) { this.imageUrl = imageUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    @Nullable
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(@Nullable Timestamp timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return isRead; } // Getter cho boolean là "is..."
    public void setRead(boolean read) { isRead = read; }
    @Nullable
    public OfferNotificationDetails getOfferDetails() { return offerDetails; }
    public void setOfferDetails(@Nullable OfferNotificationDetails offerDetails) { this.offerDetails = offerDetails; }
}