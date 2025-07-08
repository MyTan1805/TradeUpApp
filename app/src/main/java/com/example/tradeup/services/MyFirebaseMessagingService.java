// File: src/main/java/com/example/tradeup/services/MyFirebaseMessagingService.java
package com.example.tradeup.services;

import static java.security.AccessController.getContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.tradeup.R;
import com.example.tradeup.core.utils.FcmTokenUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Được gọi khi một token mới được tạo ra (khi cài app, xóa cache, hoặc token cũ hết hạn).
     * Đây là lúc chúng ta cần gửi token này lên Firestore để lưu lại.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FcmTokenUtil.saveFcmTokenToFirestore(userId, token);
        } else {
            Log.w(TAG, "No authenticated user found when refreshing token.");
        }
    }

    /**
     * Được gọi khi có một tin nhắn (thông báo) đến khi ứng dụng đang ở foreground (đang mở).
     * Khi app ở background, hệ thống sẽ tự hiển thị thông báo.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // Tạo và hiển thị thông báo
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "default_channel_id";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(1, builder.build());
        }

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message Data: " + remoteMessage.getData());
        }
    }
    /**
     * Gửi token lên server (Firestore) để lưu vào document của người dùng hiện tại.
     * @param token FCM token mới của thiết bị.
     */
}