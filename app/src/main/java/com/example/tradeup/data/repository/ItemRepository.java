// File: src/main/java/com/example/tradeup/data/repository/ItemRepository.java

package com.example.tradeup.data.repository;

import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;

import java.util.List;

public interface ItemRepository {
    void addItem(Item item, Callback<String> callback);
    void getItemById(String itemId, Callback<Item> callback);
    void getAllItems(long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback);
    void getItemsBySeller(String sellerId, long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback);
    void updateItem(Item item, Callback<Void> callback);
    void deleteItem(String itemId, Callback<Void> callback);
    void updateItemStatus(String itemId, String newStatus, Callback<Void> callback);
    void getItemsBySellerId(String sellerId, Callback<List<Item>> callback);

    // *** THÊM HÀM MỚI NÀY ***

    void searchItems(@Nullable String keyword,
                     @Nullable String categoryId,
                     @Nullable Double minPrice,
                     @Nullable Double maxPrice,
                     @Nullable String condition,
                     Callback<List<Item>> callback);
}