package com.example.tradeup.data.model.config;

public class ReportReasonConfig {
    private String id;
    private String name;

    public ReportReasonConfig() {
        this.id = "";
        this.name = "";
    }

    public ReportReasonConfig(String id, String name) {
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