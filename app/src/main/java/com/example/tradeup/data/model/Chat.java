package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    @DocumentId
    private String chatId;
    private List<String> participants;
    private Map<String, ParticipantInfoDetail> participantInfo;
    @Nullable
    private String lastMessageText;
    @ServerTimestamp
    @Nullable
    private Timestamp lastMessageTimestamp;
    @Nullable
    private String lastMessageSenderId;
    private Map<String, Integer> unreadCount;
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;
    @ServerTimestamp
    @Nullable
    private Timestamp updatedAt;
    @Nullable
    private String relatedItemId;

    // Constructor rỗng cần thiết cho Firestore
    public Chat() {
        this.chatId = "";
        this.participants = new ArrayList<>();
        this.participantInfo = new HashMap<>();
        this.lastMessageText = null;
        this.lastMessageTimestamp = null;
        this.lastMessageSenderId = null;
        this.unreadCount = new HashMap<>();
        this.createdAt = null;
        this.updatedAt = null;
        this.relatedItemId = null;
    }

    // Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public Map<String, ParticipantInfoDetail> getParticipantInfo() { return participantInfo; }
    public void setParticipantInfo(Map<String, ParticipantInfoDetail> participantInfo) { this.participantInfo = participantInfo; }
    @Nullable
    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(@Nullable String lastMessageText) { this.lastMessageText = lastMessageText; }
    @Nullable
    public Timestamp getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(@Nullable Timestamp lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
    @Nullable
    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(@Nullable String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }
    public Map<String, Integer> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Map<String, Integer> unreadCount) { this.unreadCount = unreadCount; }
    @Nullable
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }
    @Nullable
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@Nullable Timestamp updatedAt) { this.updatedAt = updatedAt; }
    @Nullable
    public String getRelatedItemId() { return relatedItemId; }
    public void setRelatedItemId(@Nullable String relatedItemId) { this.relatedItemId = relatedItemId; }
}