package com.example.tradeup.data.repository;

import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
// Không nên import ViewModel vào Repository
// import com.example.tradeup.ui.search.SearchViewModel;

import java.util.List;

public interface ItemRepository {
    void addItem(Item item, Callback<String> callback);

    void getItemById(String itemId, Callback<Item> callback);

    void getAllItems(long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback);

    void getItemsBySellerId(String sellerId, Callback<List<Item>> callback);

    void updateItem(Item item, Callback<Void> callback);

    void deleteItem(String itemId, Callback<Void> callback);

    void updateItemStatus(String itemId, String newStatus, Callback<Void> callback);

    // << SỬA LỖI: Loại bỏ các tham số GeoLocation và SortOrder khỏi hàm searchItems >>
    // ViewModel sẽ xử lý việc sắp xếp sau khi nhận dữ liệu nếu cần
    void searchItems(
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable String conditionId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            long limit,
            Callback<List<Item>> callback
    );

    // Hàm này không cần callback vì nó là "fire-and-forget"
    void incrementItemViews(String itemId);

    // Hàm này cũng là fire-and-forget
    void incrementItemOffers(String itemId);
}