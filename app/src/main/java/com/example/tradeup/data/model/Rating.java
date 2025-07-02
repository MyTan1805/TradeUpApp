package com.example.tradeup.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects; // << Import Objects để dùng trong equals/hashCode

public class Rating {
    @DocumentId
    private String ratingId;
    private String transactionId;
    private String itemId;
    private String ratedUserId;
    private String raterUserId;
    private String raterDisplayName;
    @Nullable
    private String raterProfilePictureUrl;
    private int stars; // 1-5
    @Nullable
    private String feedbackText;
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;

    // << FIX 1: Thêm constructor rỗng cần thiết cho Firestore >>
    public Rating() {
        // Firestore cần constructor này để deserialize
    }

    // Constructor đầy đủ để có thể tạo đối tượng một cách thủ công nếu cần
    public Rating(String transactionId, String itemId, String ratedUserId, String raterUserId, String raterDisplayName, int stars, @Nullable String feedbackText) {
        this.transactionId = transactionId;
        this.itemId = itemId;
        this.ratedUserId = ratedUserId;
        this.raterUserId = raterUserId;
        this.raterDisplayName = raterDisplayName;
        this.stars = stars;
        this.feedbackText = feedbackText;
    }


    // --- GETTERS AND SETTERS ---
    // (Phần này của bạn đã đúng, giữ nguyên)
    public String getRatingId() { return ratingId; }
    public void setRatingId(String ratingId) { this.ratingId = ratingId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getRatedUserId() { return ratedUserId; }
    public void setRatedUserId(String ratedUserId) { this.ratedUserId = ratedUserId; }
    public String getRaterUserId() { return raterUserId; }
    public void setRaterUserId(String raterUserId) { this.raterUserId = raterUserId; }
    public String getRaterDisplayName() { return raterDisplayName; }
    public void setRaterDisplayName(String raterDisplayName) { this.raterDisplayName = raterDisplayName; }
    @Nullable public String getRaterProfilePictureUrl() { return raterProfilePictureUrl; }
    public void setRaterProfilePictureUrl(@Nullable String raterProfilePictureUrl) { this.raterProfilePictureUrl = raterProfilePictureUrl; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    @Nullable public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(@Nullable String feedbackText) { this.feedbackText = feedbackText; }
    @Nullable public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }


    // Hàm tiện ích để định dạng ngày (Phần này của bạn đã đúng)
    @Exclude
    public String getFormattedDate() {
        if (createdAt == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(createdAt.toDate());
    }

    // << FIX 2: Thêm equals() và hashCode() để ListAdapter hoạt động đúng >>
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return stars == rating.stars &&
                Objects.equals(ratingId, rating.ratingId) &&
                Objects.equals(transactionId, rating.transactionId) &&
                Objects.equals(raterDisplayName, rating.raterDisplayName) &&
                Objects.equals(feedbackText, rating.feedbackText) &&
                Objects.equals(createdAt, rating.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ratingId, transactionId, raterDisplayName, stars, feedbackText, createdAt);
    }
}