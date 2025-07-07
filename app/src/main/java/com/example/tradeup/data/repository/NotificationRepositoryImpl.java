package com.example.tradeup.data.repository;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Notification;
import com.example.tradeup.data.source.remote.FirebaseNotificationSource;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationRepositoryImpl implements NotificationRepository {
    private final FirebaseNotificationSource source;

    @Inject
    public NotificationRepositoryImpl(FirebaseNotificationSource source) { this.source = source; }

    @Override
    public void getNotifications(String userId, int limit, Callback<List<Notification>> callback) {
        source.getNotificationsForUser(userId, limit)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void createNotification(Notification notification, Callback<Void> callback) {
        source.createNotification(notification)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void markNotificationAsRead(String notificationId, Callback<Void> callback) {
        source.markAsRead(notificationId)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}