package com.example.tradeup.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

// 1. THÊM implements Parcelable
public class ItemLocation implements Parcelable {
    private double latitude;
    private double longitude;
    private String addressString;
    @Nullable
    private String geohash;

    // Constructor rỗng
    public ItemLocation() {
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.addressString = "";
        this.geohash = null;
    }

    // Constructor đầy đủ (giữ nguyên)
    public ItemLocation(double latitude, double longitude, String addressString, @Nullable String geohash) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressString = addressString;
        this.geohash = geohash;
    }


    // ==========================================================
    // === PHẦN PARCELABLE ĐƯỢC THÊM VÀO ========================
    // ==========================================================
    protected ItemLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        addressString = in.readString();
        geohash = in.readString();
    }

    public static final Creator<ItemLocation> CREATOR = new Creator<ItemLocation>() {
        @Override
        public ItemLocation createFromParcel(Parcel in) {
            return new ItemLocation(in);
        }

        @Override
        public ItemLocation[] newArray(int size) {
            return new ItemLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(addressString);
        dest.writeString(geohash);
    }
    // ==========================================================


    // Getters and Setters (giữ nguyên của bạn)
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    @Nullable
    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(@Nullable String geohash) {
        this.geohash = geohash;
    }
}