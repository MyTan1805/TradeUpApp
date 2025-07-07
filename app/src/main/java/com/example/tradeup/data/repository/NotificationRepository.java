package com.example.tradeup.data.repository;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Notification;
import java.util.List;

public interface NotificationRepository {
    void getNotifications(String userId, int limit, Callback<List<Notification>> callback);
    void createNotification(Notification notification, Callback<Void> callback);
    void markNotificationAsRead(String notificationId, Callback<Void> callback);
}