package com.example.tradeup.data.model.config;

import androidx.annotation.Nullable;

public class DisplayCategoryConfig {
    private String id;
    private String name;
    @Nullable
    private String iconUrl;

    public DisplayCategoryConfig() {} // Constructor rỗng

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    @Nullable public String getIconUrl() { return iconUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setIconUrl(@Nullable String iconUrl) { this.iconUrl = iconUrl; }
}