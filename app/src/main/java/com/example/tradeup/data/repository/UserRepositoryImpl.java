package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.source.remote.FirestoreUserSource;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {

    private final FirestoreUserSource firestoreUserSource;

    @Inject
    public UserRepositoryImpl(FirestoreUserSource firestoreUserSource) {
        this.firestoreUserSource = firestoreUserSource;
    }

    @Override
    public void createUserProfile(@NonNull User user, final Callback<Void> callback) {
        firestoreUserSource.createUserProfile(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getUserProfile(String uid, final Callback<User> callback) {
        firestoreUserSource.getUserProfile(uid)
                .addOnSuccessListener(callback::onSuccess) // User có thể là null
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateUserProfile(@NonNull User user, final Callback<Void> callback) {
        firestoreUserSource.updateUserProfile(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}