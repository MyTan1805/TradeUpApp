package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item; // Model Item (Java)

import java.util.List;

public interface ItemRepository {
    void addItem(Item item, Callback<String> callback); // Trả về itemId

    void getItemById(String itemId, Callback<Item> callback); // Item có thể null nếu không tìm thấy, xử lý trong Callback

    void getAllItems(long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback);

    void getItemsBySeller(String sellerId, long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback);

    void updateItem(Item item, Callback<Void> callback);

    void deleteItem(String itemId, Callback<Void> callback);

    void updateItemStatus(String itemId, String newStatus, Callback<Void> callback);

    void getItemsBySellerId(String sellerId, Callback<List<Item>> callback); // Lấy tất cả item của seller
}