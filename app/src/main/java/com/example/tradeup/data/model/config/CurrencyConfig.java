package com.example.tradeup.data.model.config;

public class CurrencyConfig {
    private String code; // "VND"
    private String name; // "Việt Nam Đồng"
    private String symbol; // "₫"

    public CurrencyConfig() {
        this.code = "";
        this.name = "";
        this.symbol = "";
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}