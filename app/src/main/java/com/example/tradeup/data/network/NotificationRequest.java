package com.example.tradeup.data.network;

import java.util.Map;

public class NotificationRequest {
    public String userId;
    public String title;
    public String body;
    public Map<String, String> data;

    public NotificationRequest(String userId, String title, String body, Map<String, String> data) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.data = data;
    }
}