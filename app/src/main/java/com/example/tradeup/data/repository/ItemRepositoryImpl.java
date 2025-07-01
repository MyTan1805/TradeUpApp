// File: src/main/java/com/example/tradeup/data/repository/ItemRepositoryImpl.java

package com.example.tradeup.data.repository;

import android.util.Log;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.source.remote.FirebaseItemSource;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemRepositoryImpl implements ItemRepository {
    private static final String TAG = "ItemRepositoryImpl";

    private final FirebaseItemSource firebaseItemSource;

    @Inject
    public ItemRepositoryImpl(FirebaseItemSource firebaseItemSource) {
        this.firebaseItemSource = firebaseItemSource;
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN ---
    @Override
    public void getItemsBySellerId(String sellerId, final Callback<List<Item>> callback) {
        Log.d(TAG, "getItemsBySellerId called for sellerId: " + sellerId);
        firebaseItemSource.getItemsBySellerIdFromSource(sellerId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void searchItems(
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            @Nullable String condition,
            final Callback<List<Item>> callback
    ) {
        Log.d(TAG, "searchItems called with keyword: " + keyword + ", categoryId: " + categoryId +
                ", minPrice: " + minPrice + ", maxPrice: " + maxPrice + ", condition: " + condition);
        firebaseItemSource.searchItems(keyword, categoryId, minPrice, maxPrice, condition)
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
                .addOnSuccessListener(callback::onSuccess)
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