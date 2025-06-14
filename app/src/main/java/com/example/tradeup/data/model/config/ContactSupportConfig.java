package com.example.tradeup.data.model.config;

import androidx.annotation.Nullable;

public class ContactSupportConfig {
    @Nullable
    private String email;
    @Nullable
    private String phone;
    @Nullable
    private String faqUrl;

    public ContactSupportConfig() {
        this.email = null;
        this.phone = null;
        this.faqUrl = null;
    }

    // Getters and Setters
    @Nullable
    public String getEmail() { return email; }
    public void setEmail(@Nullable String email) { this.email = email; }
    @Nullable
    public String getPhone() { return phone; }
    public void setPhone(@Nullable String phone) { this.phone = phone; }
    @Nullable
    public String getFaqUrl() { return faqUrl; }
    public void setFaqUrl(@Nullable String faqUrl) { this.faqUrl = faqUrl; }
}