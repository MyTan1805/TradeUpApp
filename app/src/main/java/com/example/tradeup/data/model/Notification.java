// File: src/main/java/com/example/tradeup/data/model/Notification.java
package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Objects;

public class Notification {

    @DocumentId
    private String id; // <-- Sẽ được Firestore tự động điền giá trị của Document ID

    private String userId; // ID của người nhận thông báo
    private String type;   // Loại thông báo: "new_offer", "new_message", v.v.
    private String title;  // Tiêu đề của thông báo
    private String message; // Nội dung chi tiết của thông báo (trước đây có thể bạn đã đặt là 'body')

    @Nullable
    private String imageUrl; // (Tùy chọn) URL ảnh của sản phẩm hoặc người dùng liên quan

    @Nullable
    private String relatedContentId; // (Tùy chọn) ID của item, chat, hoặc user để điều hướng

    private boolean isRead = false; // Trạng thái đã đọc hay chưa

    @ServerTimestamp
    private Timestamp createdAt; // Thời gian tạo thông báo

    // Constructor rỗng bắt buộc cho Firestore
    public Notification() {}

    // ===================================================================
    // === GETTERS AND SETTERS - Đã được chuẩn hóa để khớp với Adapter ===
    // ===================================================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // << GETTER CHO NỘI DUNG THÔNG BÁO >>
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@Nullable String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Nullable
    public String getRelatedContentId() {
        return relatedContentId;
    }

    public void setRelatedContentId(@Nullable String relatedContentId) {
        this.relatedContentId = relatedContentId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // --- equals() và hashCode() để DiffUtil hoạt động chính xác ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}