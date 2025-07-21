package com.example.tradeup.data.repository;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.List;

public interface ItemRepository {
    void addItem(Item item, Callback<String> callback);
    void getItemById(String itemId, Callback<Item> callback);
    void getAllItems(long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback);
    void getItemsBySellerId(String sellerId, Callback<List<Item>> callback);
    void updateItem(Item item, Callback<Void> callback);
    void deleteItem(String itemId, Callback<Void> callback);
    void updateItemStatus(String itemId, String newStatus, Callback<Void> callback);

    void getItemsByCategory(String categoryId, Callback<List<Item>> callback);
    void getItemsByIds(List<String> itemIds, Callback<List<Item>> callback);
    void searchByFilters(
            @Nullable String keyword, @Nullable String categoryId, @Nullable String conditionId,
            @Nullable Double minPrice, @Nullable Double maxPrice, long limit,
            @NonNull String sortField, @NonNull Query.Direction direction,
            Callback<List<Item>> callback
    );

    void searchByLocation(
            @NonNull Location center,
            int radiusInKm,
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable String conditionId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            long limit,
            @NonNull String sortField,
            @NonNull Query.Direction direction,
            @NonNull Callback<List<Item>> callback
    );

    void incrementItemViews(String itemId);

    void incrementItemOffers(String itemId);
    void incrementItemChats(String itemId);
}