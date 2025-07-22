// File: app/src/main/java/com/example/tradeup/services/MyFirebaseMessagingService.java
// << PHIÊN BẢN ĐÃ SỬA LỖI VÀ HOÀN THIỆN >>
package com.example.tradeup.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.tradeup.R;
import com.example.tradeup.core.utils.FcmTokenUtil; // << SỬ DỤNG LỚP TIỆN ÍCH CHO NHẤT QUÁN
import com.example.tradeup.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Được gọi khi nhận được tin nhắn.
     * @param remoteMessage Đối tượng chứa thông tin người gửi và dữ liệu.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // **SỬA LỖI LOGIC QUAN TRỌNG NHẤT**
        // FCM hoạt động khác nhau tùy trạng thái app:
        // 1. App đang mở (Foreground): Cả notification và data payload đều vào onMessageReceived.
        // 2. App chạy nền/đã tắt (Background/Killed):
        //    - Nếu chỉ có data payload -> vào onMessageReceived.
        //    - Nếu có cả notification VÀ data payload -> notification payload sẽ tự động hiển thị,
        //      và data payload sẽ được gửi kèm trong Intent khi người dùng nhấn vào thông báo.
        //      onMessageReceived sẽ KHÔNG được gọi trong trường hợp này.
        //
        // => Do đó, chúng ta cần đảm bảo logic xử lý là nhất quán.
        //    Cách tốt nhất là luôn lấy thông tin từ notification payload (nếu có) VÀ
        //    luôn xử lý data payload để tạo intent.

        String title = null;
        String body = null;
        Map<String, String> data = remoteMessage.getData();

        // Ưu tiên lấy title và body từ notification payload nếu có
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);
        }

        // Nếu không có title/body từ notification payload (trường hợp chỉ có data payload),
        // có thể lấy từ data payload nếu server của bạn gửi.
        if (title == null && data.containsKey("title")) {
            title = data.get("title");
        }
        if (body == null && data.containsKey("body")) {
            body = data.get("body");
        }

        // Nếu cuối cùng vẫn không có gì để hiển thị, thì bỏ qua
        if (title == null || body == null) {
            Log.d(TAG, "Received a data message without title/body. Not showing notification.");
            return;
        }

        // Gọi hàm hiển thị thông báo duy nhất
        sendNotification(title, body, data);
    }

    /**
     * Được gọi khi token FCM được làm mới.
     * @param token Token mới.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token refreshed: " + token);
        // Gửi token mới lên Firestore
        String userId = (FirebaseAuth.getInstance().getCurrentUser() != null)
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            FcmTokenUtil.saveFcmTokenToFirestore(userId, token);
        }
    }

    /**
     * Tạo và hiển thị một thông báo đơn giản.
     * @param title Title của thông báo.
     * @param messageBody Nội dung của thông báo.
     * @param data Dữ liệu đi kèm để xử lý deep link.
     */
    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        // Tạo Intent để mở MainActivity khi người dùng nhấn vào
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // *** SỬA LỖI PENDINGINTENT: Đưa toàn bộ data payload vào extras ***
        // MainActivity sẽ đọc các extras này để biết cần điều hướng đi đâu.
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        // Tạo PendingIntent. Flag IMMUTABLE là bắt buộc từ Android 12 (API 31).
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // *** CẢI TIẾN: Lấy Channel ID từ string resources ***
        String channelId = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        // *** CẢI TIẾN: Sử dụng icon chuẩn cho notification ***
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody)) // Hiển thị đầy đủ nội dung dài
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao để hiển thị head-up
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Kể từ Android O (API 26), notification channel là bắt buộc.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "TradeUp Notifications", // Tên channel hiển thị cho người dùng trong cài đặt
                    NotificationManager.IMPORTANCE_HIGH); // Dùng IMPORTANCE_HIGH để có âm thanh và head-up
            notificationManager.createNotificationChannel(channel);
        }

        // Sử dụng ID ngẫu nhiên để đảm bảo các thông báo không ghi đè lên nhau
        int notificationId = new Random().nextInt();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}