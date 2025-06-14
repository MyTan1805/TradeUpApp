package com.example.tradeup.data.model.config;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CategoryConfig {
    private String id;
    private String name;
    @Nullable
    private String iconUrl;
    private List<SubcategoryConfig> subcategories;

    // Constructor rỗng cần thiết cho Firestore
    public CategoryConfig() {
        this.id = "";
        this.name = "";
        this.iconUrl = null;
        this.subcategories = new ArrayList<>();
    }

    // Constructor đầy đủ
    public CategoryConfig(String id, String name, @Nullable String iconUrl, List<SubcategoryConfig> subcategories) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.subcategories = subcategories;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(@Nullable String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<SubcategoryConfig> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<SubcategoryConfig> subcategories) {
        this.subcategories = subcategories;
    }
}