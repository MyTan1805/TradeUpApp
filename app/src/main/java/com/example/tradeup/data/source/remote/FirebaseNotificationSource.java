// File: src/main/java/com/example/tradeup/data/source/remote/FirebaseNotificationSource.java
package com.example.tradeup.data.source.remote;

import com.example.tradeup.data.model.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

public class FirebaseNotificationSource {
    private final CollectionReference notificationsCollection;

    @Inject
    public FirebaseNotificationSource(FirebaseFirestore firestore) {
        this.notificationsCollection = firestore.collection("notifications");
    }

    public Task<List<Notification>> getNotificationsForUser(String userId, int limit) {
        return notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Notification.class));
    }

    //TODO: Thêm các hàm markAsRead, markAllAsRead sau...

    public Task<Void> createNotification(Notification notification) {
        // add() sẽ tự động tạo document ID và @ServerTimestamp sẽ tự điền createdAt
        return notificationsCollection.add(notification).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            // Chuyển Task<DocumentReference> thành Task<Void> vì chúng ta không cần trả về gì cả
            return Tasks.forResult(null);
        });
    }

    public Task<Void> markAsRead(String notificationId) {
        if (notificationId == null || notificationId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Notification ID cannot be null or empty."));
        }
        return notificationsCollection.document(notificationId).update("read", true); // Tên trường là "read" vì model là isRead()
    }


}