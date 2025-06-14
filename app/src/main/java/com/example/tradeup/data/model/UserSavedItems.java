package com.example.tradeup.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.FieldValue; // Cần cho việc set server timestamp nếu không dùng @ServerTimestamp
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserSavedItems {
    @DocumentId
    private String userId; // Document ID là userId
    private List<String> itemIds;
    @ServerTimestamp
    private Timestamp updatedAt;

    // Constructor rỗng cần thiết cho Firestore
    public UserSavedItems() {
        this.itemIds = new ArrayList<>(); // Khởi tạo để tránh NullPointerException
    }

    // Constructor để tạo mới (ví dụ khi document chưa tồn tại)
    public UserSavedItems(String userId, List<String> itemIds) {
        this.userId = userId;
        this.itemIds = itemIds != null ? new ArrayList<>(itemIds) : new ArrayList<>();
        // updatedAt sẽ được set bởi @ServerTimestamp
    }

    // Getters
    public String getUserId() { return userId; }
    public List<String> getItemIds() {
        // Trả về một bản sao để tránh thay đổi từ bên ngoài nếu list là mutable
        return itemIds != null ? new ArrayList<>(itemIds) : new ArrayList<>();
    }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds != null ? new ArrayList<>(itemIds) : new ArrayList<>();
    }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; } // Firestore sẽ ghi đè

    // Phương thức tiện ích để chuyển đổi sang Map cho việc set() document nếu cần
    // (đặc biệt hữu ích nếu bạn muốn set cả FieldValue.serverTimestamp() thủ công)
    public Map<String, Object> toMapForSet() {
        Map<String, Object> map = new HashMap<>();
        // Không cần set userId vì nó là ID của document
        map.put("itemIds", this.itemIds != null ? this.itemIds : FieldValue.arrayUnion()); // Đảm bảo là array
        map.put("updatedAt", FieldValue.serverTimestamp());
        return map;
    }
}