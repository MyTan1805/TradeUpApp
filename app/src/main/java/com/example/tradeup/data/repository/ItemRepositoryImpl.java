package com.example.tradeup.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.source.remote.FirebaseItemSource;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemRepositoryImpl implements ItemRepository {
    private static final String TAG = "ItemRepositoryImpl"; // Tag cho logging

    private final FirebaseItemSource firebaseItemSource;

    @Inject
    public ItemRepositoryImpl(FirebaseItemSource firebaseItemSource) {
        this.firebaseItemSource = firebaseItemSource;
    }

    @Override
    public void getItemsBySellerId(String sellerId, final Callback<List<Item>> callback) {
        Log.d(TAG, "getItemsBySellerId called for sellerId: " + sellerId);
        firebaseItemSource.getItemsBySellerIdFromSource(sellerId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void addItem(Item item, final Callback<String> callback) {
        firebaseItemSource.addItem(item)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getItemById(String itemId, final Callback<Item> callback) {
        firebaseItemSource.getItemById(itemId)
                .addOnSuccessListener(item -> {
                    if (item != null) {
                        callback.onSuccess(item);
                    } else {
                        // Xử lý trường hợp không tìm thấy item, có thể gọi onFailure
                        // hoặc callback.onSuccess(null) nếu Callback<Item> cho phép null.
                        // Hiện tại Callback<T> không có onNotFound, nên có thể coi là một dạng lỗi
                        // hoặc để ViewModel/Fragment xử lý null.
                        callback.onSuccess(null); // Hoặc callback.onFailure(new Exception("Item not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getAllItems(long limit, @Nullable String lastVisibleItemId, final Callback<List<Item>> callback) {
        firebaseItemSource.getAllItems(limit, lastVisibleItemId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getItemsBySeller(String sellerId, long limit, @Nullable String lastVisibleItemId, final Callback<List<Item>> callback) {
        Log.d(TAG, "getItemsBySeller (paginated) called for sellerId: " + sellerId);
        firebaseItemSource.getItemsBySeller(sellerId, limit, lastVisibleItemId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateItem(Item item, final Callback<Void> callback) {
        firebaseItemSource.updateItem(item)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void deleteItem(String itemId, final Callback<Void> callback) {
        firebaseItemSource.deleteItem(itemId)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateItemStatus(String itemId, String newStatus, final Callback<Void> callback) {
        firebaseItemSource.updateItemStatus(itemId, newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}