// File: src/main/java/com/example/tradeup/services/MyFirebaseMessagingService.java
package com.example.tradeup.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
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
        sendRegistrationToServer(token);
    }

    /**
     * Được gọi khi có một tin nhắn (thông báo) đến khi ứng dụng đang ở foreground (đang mở).
     * Khi app ở background, hệ thống sẽ tự hiển thị thông báo.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // In ra để debug
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Trong app thực tế, bạn có thể tạo một thông báo tùy chỉnh ở đây
        // để hiển thị ngay cả khi app đang mở, hoặc cập nhật một badge trên icon.
    }

    /**
     * Gửi token lên server (Firestore) để lưu vào document của người dùng hiện tại.
     * @param token FCM token mới của thiết bị.
     */
    private void sendRegistrationToServer(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Chỉ gửi token lên nếu người dùng đã đăng nhập
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Dùng Map để cập nhật, chỉ thêm token mới vào mảng fcmTokens
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmTokens", FieldValue.arrayUnion(token));

            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "FCM token updated successfully for user: " + userId))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating FCM token", e));
        }
    }
}