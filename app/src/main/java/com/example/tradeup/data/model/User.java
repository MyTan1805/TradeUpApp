package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.List;
import java.util.ArrayList;

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
    private ContactInfo contactInfo; // Giả sử bạn có class ContactInfo
    private double averageRating;
    private long totalRatingCount;
    private double sumOfStars;
    private int totalTransactions;
    private int totalListings;
    private List<String> blockedUsers;
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
    private boolean deactivated;
    @Nullable
    private Timestamp lastLoginAt;
    private long reviewCount;

    private String displayName_lowercase;

    private String role;

    // Constructor rỗng cần thiết cho Firestore
    public User() {
        this.uid = "";
        this.email = "";
        this.displayName = "";
        this.averageRating = 0.0;
        this.totalRatingCount = 0L;
        this.sumOfStars = 0.0;
        this.totalTransactions = 0;
        this.totalListings = 0;
        this.reviewCount = 0L;
        this.deactivated = false;
        this.blockedUsers = new ArrayList<>();
        this.role = "user";
        // Các trường nullable mặc định là null
    }

    // Getters and Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        if (displayName != null) {
            this.displayName_lowercase = displayName.toLowerCase(); // Tự động tạo
        }
    }

    public String getDisplayName_lowercase() { return displayName_lowercase; }
    public void setDisplayName_lowercase(String displayName_lowercase) { this.displayName_lowercase = displayName_lowercase; }

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

    public long getTotalRatingCount() { return totalRatingCount; }
    public void setTotalRatingCount(long totalRatingCount) { this.totalRatingCount = totalRatingCount; }

    public double getSumOfStars() { return sumOfStars; }
    public void setSumOfStars(double sumOfStars) { this.sumOfStars = sumOfStars; }

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

    public boolean isDeactivated() { return deactivated; }
    public void setDeactivated(boolean deactivated) { deactivated = deactivated; }

    @Nullable
    public Timestamp getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(@Nullable Timestamp lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // === FIX: THÊM GETTER/SETTER CHO reviewCount ===
    public long getReviewCount() { return reviewCount; }
    public void setReviewCount(long reviewCount) { this.reviewCount = reviewCount; }



    public List<String> getBlockedUsers() {
        return blockedUsers != null ? blockedUsers : new ArrayList<>();
    }
    public void setBlockedUsers(List<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }
}