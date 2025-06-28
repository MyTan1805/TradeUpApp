// File: src/main/java/com/example/tradeup/data/repository/UserRepositoryImpl.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import javax.inject.Inject;

public class UserRepositoryImpl implements UserRepository {

    private final FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";

    @Inject
    public UserRepositoryImpl(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void getUserProfile(String uid, @NonNull Callback<User> callback) {
        db.collection(USERS_COLLECTION).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.toObject(User.class));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateUserProfile(String uid, Map<String, Object> updates, @NonNull Callback<Void> callback) {
        db.collection(USERS_COLLECTION).document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // ... các phương thức khác ...
    @Override
    public void createUserProfile(@NonNull User user, @NonNull Callback<Void> callback) {
        // ...
    }
}