// File: src/main/java/com/example/tradeup/data/repository/UserRepositoryImpl.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.User;
// << FIX: Cần import FirestoreUserSource >>
import com.example.tradeup.data.source.remote.FirestoreUserSource;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton // << Rất nên có @Singleton cho Repository >>
public class UserRepositoryImpl implements UserRepository {

    // << FIX: Inject FirestoreUserSource thay vì FirebaseFirestore >>
    private final FirestoreUserSource firestoreUserSource;

    @Inject
    public UserRepositoryImpl(FirestoreUserSource firestoreUserSource) {
        this.firestoreUserSource = firestoreUserSource;
    }

    @Override
    public void createUserProfile(@NonNull User user, @NonNull Callback<Void> callback) {
        firestoreUserSource.createUserProfile(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getUserProfile(String uid, @NonNull Callback<User> callback) {
        firestoreUserSource.getUserProfile(uid)
                .addOnSuccessListener(user -> {
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        // Trường hợp user không tồn tại trong Firestore
                        callback.onFailure(new Exception("User profile not found in database."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateUserProfile(String uid, Map<String, Object> updates, @NonNull Callback<Void> callback) {
        firestoreUserSource.updateUserFields(uid, updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // << FIX: THÊM CÁC PHƯƠNG THỨC CÒN THIẾU >>

    @Override
    public void deactivateUser(String uid, Callback<Void> callback) {
        firestoreUserSource.deactivateUser(uid)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void deleteUser(String uid, Callback<Void> callback) {
        firestoreUserSource.deleteUser(uid)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}