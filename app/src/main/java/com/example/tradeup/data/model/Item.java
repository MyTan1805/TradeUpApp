package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.List;

public class Item {
    @DocumentId
    private String itemId;
    private String sellerId;
    private String sellerDisplayName;
    @Nullable
    private String sellerProfilePictureUrl;
    private String title;
    private String description;
    private double price;
    private boolean isNegotiable;
    private String category; // ID from appConfig
    @Nullable
    private String subCategory; // ID from appConfig
    private String condition; // ID from appConfig
    private ItemLocation location;
    private List<String> imageUrls;
    private String status; // "available", "sold", "paused", "deleted"
    @Nullable
    private String itemBehavior; // e.g., "pickup_only"
    @Nullable
    private List<String> tags;
    private int viewsCount;
    private int offersCount;
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;
    @ServerTimestamp
    @Nullable
    private Timestamp updatedAt;
    @Nullable
    private String soldToUserId;
    @Nullable
    private Timestamp soldAt;

    // Constructor rỗng cần thiết cho Firestore
    public Item() {
        this.itemId = "";
        this.sellerId = "";
        this.sellerDisplayName = "";
        this.sellerProfilePictureUrl = null;
        this.title = "";
        this.description = "";
        this.price = 0.0;
        this.isNegotiable = false;
        this.category = "";
        this.subCategory = null;
        this.condition = "";
        this.location = new ItemLocation();
        this.imageUrls = new ArrayList<>();
        this.status = "available";
        this.itemBehavior = null;
        this.tags = null; // hoặc new ArrayList<>() nếu bạn muốn mặc định là list rỗng
        this.viewsCount = 0;
        this.offersCount = 0;
        this.createdAt = null;
        this.updatedAt = null;
        this.soldToUserId = null;
        this.soldAt = null;
    }

    // Getters and Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getSellerDisplayName() { return sellerDisplayName; }
    public void setSellerDisplayName(String sellerDisplayName) { this.sellerDisplayName = sellerDisplayName; }
    @Nullable
    public String getSellerProfilePictureUrl() { return sellerProfilePictureUrl; }
    public void setSellerProfilePictureUrl(@Nullable String sellerProfilePictureUrl) { this.sellerProfilePictureUrl = sellerProfilePictureUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isNegotiable() { return isNegotiable; }
    public void setNegotiable(boolean negotiable) { isNegotiable = negotiable; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    @Nullable
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(@Nullable String subCategory) { this.subCategory = subCategory; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public ItemLocation getLocation() { return location; }
    public void setLocation(ItemLocation location) { this.location = location; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Nullable
    public String getItemBehavior() { return itemBehavior; }
    public void setItemBehavior(@Nullable String itemBehavior) { this.itemBehavior = itemBehavior; }
    @Nullable
    public List<String> getTags() { return tags; }
    public void setTags(@Nullable List<String> tags) { this.tags = tags; }
    public int getViewsCount() { return viewsCount; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }
    public int getOffersCount() { return offersCount; }
    public void setOffersCount(int offersCount) { this.offersCount = offersCount; }
    @Nullable
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }
    @Nullable
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@Nullable Timestamp updatedAt) { this.updatedAt = updatedAt; }
    @Nullable
    public String getSoldToUserId() { return soldToUserId; }
    public void setSoldToUserId(@Nullable String soldToUserId) { this.soldToUserId = soldToUserId; }
    @Nullable
    public Timestamp getSoldAt() { return soldAt; }
    public void setSoldAt(@Nullable Timestamp soldAt) { this.soldAt = soldAt; }
}