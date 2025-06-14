package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.User; // Model User (Java)
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        // Firestore sẽ tự xử lý @ServerTimestamp trong POJO User khi thêm mới
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
                    return documentSnapshot.toObject(User.class); // Sẽ là null nếu không tồn tại
                });
    }

    public Task<Void> updateUserProfile(@NonNull User user) {
        if (user.getUid() == null || user.getUid().trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User UID cannot be blank for update"));
        }
        // Dùng set(user) để ghi đè toàn bộ, đảm bảo các trường null trong user POJO sẽ xóa field đó trên Firestore (nếu không có @Exclude)
        // Hoặc bạn có thể xây dựng một Map<String, Object> để chỉ cập nhật các trường cụ thể nếu muốn.
        return usersCollection.document(user.getUid()).set(user);
    }
}