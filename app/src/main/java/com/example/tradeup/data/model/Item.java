package com.example.tradeup.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Item implements Parcelable {
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
    private String category;
    @Nullable
    private String subCategory;
    private String condition;
    private ItemLocation location;
    private List<String> imageUrls;
    private String status;
    @Nullable
    private String itemBehavior;
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

    // Constructor rỗng
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
        this.tags = null;
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

    // Phương thức getId() cho DiffUtil
    public String getId() {
        return itemId;
    }

    // equals() và hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Double.compare(item.price, price) == 0 && isNegotiable == item.isNegotiable && viewsCount == item.viewsCount && offersCount == item.offersCount && Objects.equals(itemId, item.itemId) && Objects.equals(sellerId, item.sellerId) && Objects.equals(sellerDisplayName, item.sellerDisplayName) && Objects.equals(sellerProfilePictureUrl, item.sellerProfilePictureUrl) && Objects.equals(title, item.title) && Objects.equals(description, item.description) && Objects.equals(category, item.category) && Objects.equals(subCategory, item.subCategory) && Objects.equals(condition, item.condition) && Objects.equals(location, item.location) && Objects.equals(imageUrls, item.imageUrls) && Objects.equals(status, item.status) && Objects.equals(itemBehavior, item.itemBehavior) && Objects.equals(tags, item.tags) && Objects.equals(createdAt, item.createdAt) && Objects.equals(updatedAt, item.updatedAt) && Objects.equals(soldToUserId, item.soldToUserId) && Objects.equals(soldAt, item.soldAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, sellerId, sellerDisplayName, sellerProfilePictureUrl, title, description, price, isNegotiable, category, subCategory, condition, location, imageUrls, status, itemBehavior, tags, viewsCount, offersCount, createdAt, updatedAt, soldToUserId, soldAt);
    }


    // ==========================================================
    // === PHẦN PARCELABLE =======================================
    // ==========================================================
    protected Item(Parcel in) {
        itemId = in.readString();
        sellerId = in.readString();
        sellerDisplayName = in.readString();
        sellerProfilePictureUrl = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        isNegotiable = in.readByte() != 0;
        category = in.readString();
        subCategory = in.readString();
        condition = in.readString();
        location = in.readParcelable(ItemLocation.class.getClassLoader());
        imageUrls = in.createStringArrayList();
        status = in.readString();
        itemBehavior = in.readString();
        tags = in.createStringArrayList();
        viewsCount = in.readInt();
        offersCount = in.readInt();

        long secondsCreated = in.readLong();
        int nanosCreated = in.readInt();
        createdAt = (secondsCreated != -1) ? new Timestamp(secondsCreated, nanosCreated) : null;

        long secondsUpdated = in.readLong();
        int nanosUpdated = in.readInt();
        updatedAt = (secondsUpdated != -1) ? new Timestamp(secondsUpdated, nanosUpdated) : null;

        soldToUserId = in.readString();

        long secondsSold = in.readLong();
        int nanosSold = in.readInt();
        soldAt = (secondsSold != -1) ? new Timestamp(secondsSold, nanosSold) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(sellerId);
        dest.writeString(sellerDisplayName);
        dest.writeString(sellerProfilePictureUrl);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeByte((byte) (isNegotiable ? 1 : 0));
        dest.writeString(category);
        dest.writeString(subCategory);
        dest.writeString(condition);
        dest.writeParcelable(location, flags);
        dest.writeStringList(imageUrls);
        dest.writeString(status);
        dest.writeString(itemBehavior);
        dest.writeStringList(tags);
        dest.writeInt(viewsCount);
        dest.writeInt(offersCount);

        dest.writeLong(createdAt != null ? createdAt.getSeconds() : -1);
        dest.writeInt(createdAt != null ? createdAt.getNanoseconds() : -1);

        dest.writeLong(updatedAt != null ? updatedAt.getSeconds() : -1);
        dest.writeInt(updatedAt != null ? updatedAt.getNanoseconds() : -1);

        dest.writeString(soldToUserId);

        dest.writeLong(soldAt != null ? soldAt.getSeconds() : -1);
        dest.writeInt(soldAt != null ? soldAt.getNanoseconds() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}