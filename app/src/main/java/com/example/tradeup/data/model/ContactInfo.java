package com.example.tradeup.data.model;

import androidx.annotation.Nullable;

public class ContactInfo {
    @Nullable
    private String phone;
    @Nullable
    private String zalo;
    @Nullable
    private String facebook;

    // Constructor rỗng cần thiết cho Firestore
    public ContactInfo() {
        // Firestore cần constructor này
    }

    public ContactInfo(@Nullable String phone, @Nullable String zalo, @Nullable String facebook) {
        this.phone = phone;
        this.zalo = zalo;
        this.facebook = facebook;
    }

    // Getters and Setters
    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    @Nullable
    public String getZalo() {
        return zalo;
    }

    public void setZalo(@Nullable String zalo) {
        this.zalo = zalo;
    }

    @Nullable
    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(@Nullable String facebook) {
        this.facebook = facebook;
    }
}