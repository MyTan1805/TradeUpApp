package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.List;

public class User {
    @DocumentId
    private String uid; // Should match Firebase Auth UID
    private String email;
    private String displayName;
    @Nullable
    private String profilePictureUrl;
    @Nullable
    private String bio;
    @Nullable
    private ContactInfo contactInfo;
    private double averageRating;
    private int totalTransactions;
    private int totalListings;
    @Nullable
    private List<String> fcmTokens; // List of FCM tokens
    @Nullable
    private GeoPoint location;
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;
    @ServerTimestamp
    @Nullable
    private Timestamp updatedAt;
    private boolean isDeactivated;
    @Nullable
    private Timestamp lastLoginAt;

    // Constructor rỗng cần thiết cho Firestore
    public User() {
        this.uid = "";
        this.email = "";
        this.displayName = "";
        this.averageRating = 0.0;
        this.totalTransactions = 0;
        this.totalListings = 0;
        this.isDeactivated = false;
        // Các trường nullable mặc định là null
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    @Nullable
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(@Nullable String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    @Nullable
    public String getBio() { return bio; }
    public void setBio(@Nullable String bio) { this.bio = bio; }

    @Nullable
    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(@Nullable ContactInfo contactInfo) { this.contactInfo = contactInfo; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public int getTotalListings() { return totalListings; }
    public void setTotalListings(int totalListings) { this.totalListings = totalListings; }

    @Nullable
    public List<String> getFcmTokens() { return fcmTokens; }
    public void setFcmTokens(@Nullable List<String> fcmTokens) { this.fcmTokens = fcmTokens; }

    @Nullable
    public GeoPoint getLocation() { return location; }
    public void setLocation(@Nullable GeoPoint location) { this.location = location; }

    @Nullable
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }

    @Nullable
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@Nullable Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeactivated() { return isDeactivated; }
    public void setDeactivated(boolean deactivated) { isDeactivated = deactivated; }

    @Nullable
    public Timestamp getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(@Nullable Timestamp lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}