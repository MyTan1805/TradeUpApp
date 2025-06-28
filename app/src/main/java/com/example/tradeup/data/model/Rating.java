package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

    // ... (constructor và các getter/setter hiện có không đổi) ...
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

    @Nullable
    public String getRaterProfilePictureUrl() { return raterProfilePictureUrl; }
    public void setRaterProfilePictureUrl(@Nullable String raterProfilePictureUrl) { this.raterProfilePictureUrl = raterProfilePictureUrl; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    @Nullable
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(@Nullable String feedbackText) { this.feedbackText = feedbackText; }

    @Nullable
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }

    // === FIX: THÊM HÀM TIỆN ÍCH ĐỂ ĐỊNH DẠNG NGÀY ===
    @Exclude // Bỏ qua trường này khi đọc/ghi vào Firestore
    public String getFormattedDate() {
        if (createdAt == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(createdAt.toDate());
    }
}