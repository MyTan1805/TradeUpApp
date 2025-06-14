package com.example.tradeup.data.model;

import androidx.annotation.Nullable;

public class ParticipantInfoDetail {
    private String displayName;
    @Nullable
    private String profilePictureUrl;

    // Constructor rỗng cần thiết cho Firestore
    public ParticipantInfoDetail() {
        this.displayName = "";
        this.profilePictureUrl = null;
    }

    public ParticipantInfoDetail(String displayName, @Nullable String profilePictureUrl) {
        this.displayName = displayName;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and Setters
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(@Nullable String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}