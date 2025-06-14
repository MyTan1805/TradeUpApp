package com.example.tradeup.data.model;

import androidx.annotation.Nullable;

public class ItemLocation {
    private double latitude;
    private double longitude;
    private String addressString;
    @Nullable
    private String geohash;

    // Constructor rỗng cần thiết cho Firestore
    public ItemLocation() {
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.addressString = "";
        this.geohash = null;
    }

    public ItemLocation(double latitude, double longitude, String addressString, @Nullable String geohash) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressString = addressString;
        this.geohash = geohash;
    }

    // Getters and Setters
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