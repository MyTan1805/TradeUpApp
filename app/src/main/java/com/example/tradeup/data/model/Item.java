// File: src/main/java/com/example/tradeup/data/model/Item.java
package com.example.tradeup.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Item implements Parcelable {
    @DocumentId
    private String itemId;

    // Các trường dữ liệu, không cần @PropertyName
    private String sellerId;
    private String sellerDisplayName;
    @Nullable
    private String sellerProfilePictureUrl;
    private String title;
    private String description;
    private double price;
    private boolean negotiable;
    private String category;
    @Nullable
    private String subCategory;
    private String condition;
    private GeoPoint location; // Dùng kiểu GeoPoint của Firestore
    private String geohash;
    private String addressString;
    private List<String> imageUrls;
    private List<String> searchKeywords;
    private String status;
    @Nullable
    private String itemBehavior;
    @Nullable
    private List<String> tags;
    private Long viewsCount; // Dùng Long
    private Long offersCount; // Dùng Long
    private Long chatsCount;
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

    public Item() {
        // Constructor rỗng để Firestore deserialize
        this.imageUrls = new ArrayList<>();
        this.searchKeywords = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.viewsCount = 0L;
        this.offersCount = 0L;
        this.chatsCount = 0L;
        this.status = "available";
    }

    // --- GETTERS VÀ SETTERS ---
    // (Không cần @PropertyName)
    public Long getChatsCount() { return chatsCount != null ? chatsCount : 0L; }
    public void setChatsCount(Long chatsCount) { this.chatsCount = chatsCount; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getSellerDisplayName() { return sellerDisplayName; }
    public void setSellerDisplayName(String sellerDisplayName) { this.sellerDisplayName = sellerDisplayName; }
    @Nullable public String getSellerProfilePictureUrl() { return sellerProfilePictureUrl; }
    public void setSellerProfilePictureUrl(@Nullable String sellerProfilePictureUrl) { this.sellerProfilePictureUrl = sellerProfilePictureUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    @Nullable public String getSubCategory() { return subCategory; }
    public void setSubCategory(@Nullable String subCategory) { this.subCategory = subCategory; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
    public String getGeohash() { return geohash; }
    public void setGeohash(String geohash) { this.geohash = geohash; }
    public String getAddressString() { return addressString; }
    public void setAddressString(String addressString) { this.addressString = addressString; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Nullable public String getItemBehavior() { return itemBehavior; }
    public void setItemBehavior(@Nullable String itemBehavior) { this.itemBehavior = itemBehavior; }
    @Nullable public List<String> getTags() { return tags; }
    public void setTags(@Nullable List<String> tags) { this.tags = tags; }
    public Long getViewsCount() { return viewsCount != null ? viewsCount : 0L; }
    public void setViewsCount(Long viewsCount) { this.viewsCount = viewsCount; }
    public Long getOffersCount() { return offersCount != null ? offersCount : 0L; }
    public void setOffersCount(Long offersCount) { this.offersCount = offersCount; }
    @Nullable public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }
    @Nullable public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@Nullable Timestamp updatedAt) { this.updatedAt = updatedAt; }
    @Nullable public String getSoldToUserId() { return soldToUserId; }
    public void setSoldToUserId(@Nullable String soldToUserId) { this.soldToUserId = soldToUserId; }
    @Nullable public Timestamp getSoldAt() { return soldAt; }
    public void setSoldAt(@Nullable Timestamp soldAt) { this.soldAt = soldAt; }

    @Exclude
    public String getId() { return itemId; }

    @Override
    public int hashCode() { return Objects.hash(itemId, title, price, status); }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId);
    }

    // --- Parcelable Implementation ---
    protected Item(Parcel in) {
        itemId = in.readString();
        sellerId = in.readString();
        sellerDisplayName = in.readString();
        sellerProfilePictureUrl = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        negotiable = in.readByte() != 0;
        category = in.readString();
        subCategory = in.readString();
        condition = in.readString();
        double lat = in.readDouble();
        double lon = in.readDouble();
        if (lat != -1000) { // Dùng giá trị đặc biệt để check null
            location = new GeoPoint(lat, lon);
        }
        geohash = in.readString();
        addressString = in.readString();
        imageUrls = in.createStringArrayList();
        searchKeywords = in.createStringArrayList();
        status = in.readString();
        itemBehavior = in.readString();
        tags = in.createStringArrayList();
        if (in.readByte() == 0) { viewsCount = null; } else { viewsCount = in.readLong(); }
        if (in.readByte() == 0) { offersCount = null; } else { offersCount = in.readLong(); }
        if (in.readByte() == 0) { chatsCount = null; } else { chatsCount = in.readLong(); }
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        updatedAt = in.readParcelable(Timestamp.class.getClassLoader());
        soldToUserId = in.readString();
        soldAt = in.readParcelable(Timestamp.class.getClassLoader());
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
        dest.writeByte((byte) (negotiable ? 1 : 0));
        dest.writeString(category);
        dest.writeString(subCategory);
        dest.writeString(condition);
        if (location != null) {
            dest.writeDouble(location.getLatitude());
            dest.writeDouble(location.getLongitude());
        } else {
            dest.writeDouble(-1000); // Giá trị đặc biệt
            dest.writeDouble(-1000);
        }
        dest.writeString(geohash);
        dest.writeString(addressString);

        dest.writeStringList(imageUrls);
        dest.writeStringList(searchKeywords);
        dest.writeString(status);
        dest.writeString(itemBehavior);
        dest.writeStringList(tags);
        if (viewsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(viewsCount);
        }
        if (offersCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(offersCount);
        }
        if (chatsCount == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeLong(chatsCount); }
        dest.writeParcelable(createdAt, flags);
        dest.writeParcelable(updatedAt, flags);
        dest.writeString(soldToUserId);
        dest.writeParcelable(soldAt, flags);
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