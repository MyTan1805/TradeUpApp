package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
// import com.example.tradeup.data.model.Item; // Bỏ qua nếu chỉ làm việc với ID
import java.util.List;

public interface UserSavedItemsRepository {
    void saveItem(@NonNull String userId, @NonNull String itemId, Callback<Void> callback);

    void unsaveItem(@NonNull String userId, @NonNull String itemId, Callback<Void> callback);

    void getSavedItemIds(@NonNull String userId, Callback<List<String>> callback);

    void isItemSaved(@NonNull String userId, @NonNull String itemId, Callback<Boolean> callback);
}