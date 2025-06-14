package com.example.tradeup.data.model.config;

import androidx.annotation.Nullable;

public class PaymentMethodConfig {
    private String id;
    private String name;
    @Nullable
    private String description;
    @Nullable
    private String iconUrl;
    private String type; // "offline", "online_gateway"
    private boolean isActive;

    public PaymentMethodConfig() {
        this.id = "";
        this.name = "";
        this.description = null;
        this.iconUrl = null;
        this.type = "";
        this.isActive = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    @Nullable
    public String getDescription() { return description; }
    public void setDescription(@Nullable String description) { this.description = description; }
    @Nullable
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(@Nullable String iconUrl) { this.iconUrl = iconUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}