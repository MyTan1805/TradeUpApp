package com.example.tradeup.data.repository;

import android.util.Log;
import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.source.remote.FirebaseItemSource;
// Không nên import ViewModel vào Repository
// import com.example.tradeup.ui.search.SearchViewModel;

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
    public void getItemsBySellerId(String sellerId, final Callback<List<Item>> callback) {
        Log.d(TAG, "getItemsBySellerId called for sellerId: " + sellerId);
        firebaseItemSource.getItemsBySellerIdFromSource(sellerId)
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

    // << SỬA LỖI: Triển khai lại hàm searchItems cho khớp với Interface mới >>
    @Override
    public void searchItems(
            @Nullable String keyword, @Nullable String categoryId, @Nullable String conditionId,
            @Nullable Double minPrice, @Nullable Double maxPrice, long limit,
            Callback<List<Item>> callback
    ) {
        firebaseItemSource.searchItems(keyword, categoryId, conditionId, minPrice, maxPrice, limit)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void incrementItemViews(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            Log.w(TAG, "Attempted to increment views with null or empty itemId.");
            return;
        }
        firebaseItemSource.incrementItemViews(itemId)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment views for item: " + itemId, e));
    }

    @Override
    public void incrementItemOffers(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            Log.w(TAG, "Attempted to increment offers with null or empty itemId.");
            return;
        }
        firebaseItemSource.incrementItemOffers(itemId)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment offers for item: " + itemId, e));
    }
}