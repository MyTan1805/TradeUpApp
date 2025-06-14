package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.source.remote.FirebaseUserSavedItemsSource;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserSavedItemsRepositoryImpl implements UserSavedItemsRepository {

    private final FirebaseUserSavedItemsSource firebaseUserSavedItemsSource;

    @Inject
    public UserSavedItemsRepositoryImpl(FirebaseUserSavedItemsSource firebaseUserSavedItemsSource) {
        this.firebaseUserSavedItemsSource = firebaseUserSavedItemsSource;
    }

    @Override
    public void saveItem(@NonNull String userId, @NonNull String itemId, final Callback<Void> callback) {
        firebaseUserSavedItemsSource.saveItem(userId, itemId)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void unsaveItem(@NonNull String userId, @NonNull String itemId, final Callback<Void> callback) {
        firebaseUserSavedItemsSource.unsaveItem(userId, itemId)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getSavedItemIds(@NonNull String userId, final Callback<List<String>> callback) {
        firebaseUserSavedItemsSource.getSavedItemIds(userId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void isItemSaved(@NonNull String userId, @NonNull String itemId, final Callback<Boolean> callback) {
        firebaseUserSavedItemsSource.isItemSaved(userId, itemId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }
}