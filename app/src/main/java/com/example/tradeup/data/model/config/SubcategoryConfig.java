package com.example.tradeup.data.model.config;

public class SubcategoryConfig {
    private String id;
    private String name;

    public SubcategoryConfig() {
        this.id = "";
        this.name = "";
    }

    public SubcategoryConfig(String id, String name) {
        this.id = id;
        this.name = name;
    }

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
}