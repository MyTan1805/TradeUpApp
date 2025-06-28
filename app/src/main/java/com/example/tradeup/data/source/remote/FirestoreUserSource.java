package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;

public class FirestoreUserSource {

    private final CollectionReference usersCollection;

    @Inject
    public FirestoreUserSource(FirebaseFirestore firestore) {
        this.usersCollection = firestore.collection("users");
    }

    public Task<Void> createUserProfile(@NonNull User user) {
        if (user.getUid() == null || user.getUid().trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User UID cannot be blank for creating profile"));
        }
        return usersCollection.document(user.getUid()).set(user);
    }

    public Task<User> getUserProfile(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User UID cannot be blank for fetching profile"));
        }
        return usersCollection.document(uid).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    DocumentSnapshot documentSnapshot = task.getResult();
                    return documentSnapshot.toObject(User.class);
                });
    }

    /**
     * <b>Phương thức 1: Ghi đè toàn bộ (Dùng `set`).</b><br>
     * Cập nhật toàn bộ document User bằng object User được cung cấp.
     * Bất kỳ trường nào có trong document trên Firestore mà không có trong object User này sẽ bị xóa.
     * <b>Sử dụng khi:</b> Bạn muốn đồng bộ hoàn toàn trạng thái từ một form "Chỉnh sửa hồ sơ" hoàn chỉnh.
     * @param user Object User hoàn chỉnh để ghi đè.
     * @return Task<Void>
     */
    public Task<Void> updateUserProfile(@NonNull User user) {
        if (user.getUid() == null || user.getUid().trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User UID cannot be blank for update"));
        }
        return usersCollection.document(user.getUid()).set(user);
    }

    /**
     * <b>Phương thức 2: Cập nhật từng phần (Dùng `update`).</b><br>
     * Chỉ cập nhật các trường được chỉ định trong Map. Các trường khác trong document sẽ không bị ảnh hưởng.
     * <b>Sử dụng khi:</b> Bạn chỉ muốn thay đổi một vài thông tin cụ thể, ví dụ như cập nhật fcmToken,
     * hoặc cập nhật lastLoginAt mà không muốn ảnh hưởng tới các thông tin khác.
     * @param uid ID của người dùng cần cập nhật.
     * @param updates Một Map chứa các cặp key-value của các trường cần cập nhật.
     * @return Task<Void>
     */
    public Task<Void> updateUserFields(@NonNull String uid, @NonNull Map<String, Object> updates) {
        if (uid.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User UID cannot be blank for update"));
        }
        // Luôn đảm bảo trường updatedAt được cập nhật
        updates.put("updatedAt", FieldValue.serverTimestamp());
        return usersCollection.document(uid).update(updates);
    }
}