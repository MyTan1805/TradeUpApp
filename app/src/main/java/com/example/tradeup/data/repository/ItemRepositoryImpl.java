package com.example.tradeup.data.repository;

import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.source.remote.FirebaseItemSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
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
    public void addItem(Item item, Callback<String> callback) {
        firebaseItemSource.addItem(item)
                .addOnSuccessListener(itemId -> { // << FIX: Đổi tên biến thành itemId
                    callback.onSuccess(itemId);
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void getItemById(String itemId, Callback<Item> callback) {
        firebaseItemSource.getItemById(itemId)
                .addOnSuccessListener(item -> { // << FIX: Đổi tên biến thành item
                    callback.onSuccess(item);
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void getAllItems(long limit, @Nullable String lastVisibleItemId, Callback<List<Item>> callback) {
        firebaseItemSource.getAllItems(limit, lastVisibleItemId)
                .addOnSuccessListener(items -> { // tên 'items' ở đây đã đúng
                    callback.onSuccess(items);
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void getItemsBySellerId(String sellerId, Callback<List<Item>> callback) {
        firebaseItemSource.getItemsBySellerIdFromSource(sellerId)
                .addOnSuccessListener(items -> { // tên 'items' ở đây đã đúng
                    callback.onSuccess(items);
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void updateItem(Item item, Callback<Void> callback) {
        firebaseItemSource.updateItem(item)
                .addOnSuccessListener(aVoid -> { // << FIX: Tham số là Void
                    callback.onSuccess(null); // << FIX: Truyền vào null
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void deleteItem(String itemId, Callback<Void> callback) {
        firebaseItemSource.deleteItem(itemId)
                .addOnSuccessListener(aVoid -> { // << FIX: Tham số là Void
                    callback.onSuccess(null); // << FIX: Truyền vào null
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void updateItemStatus(String itemId, String newStatus, Callback<Void> callback) {
        firebaseItemSource.updateItemStatus(itemId, newStatus)
                .addOnSuccessListener(aVoid -> { // << FIX: Tham số là Void
                    callback.onSuccess(null); // << FIX: Truyền vào null
                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                    callback.onComplete();
                });
    }

    @Override
    public void getItemsByCategory(String categoryId, Callback<List<Item>> callback) {
        firebaseItemSource.getItemsByCategory(categoryId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getItemsByIds(List<String> itemIds, Callback<List<Item>> callback) {
        firebaseItemSource.getItemsByIds(itemIds)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void searchByFilters(
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable String conditionId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            long limit,
            @NonNull String sortField, // << THÊM THAM SỐ NÀY
            @NonNull Query.Direction direction, // << THÊM THAM SỐ NÀY
            @NonNull Callback<List<Item>> callback
    ) {
        firebaseItemSource.searchByFilters(
                keyword, categoryId, conditionId, minPrice, maxPrice, limit,
                sortField, direction // << Truyền xuống tầng source
        ).addOnSuccessListener(items -> {
            callback.onSuccess(items);
            callback.onComplete();
        }).addOnFailureListener(e -> {
            callback.onFailure(e);
            callback.onComplete();
        });
    }

    @Override
    public void searchByLocation(
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
    ) {
        // Gọi xuống source với đầy đủ tham số
        firebaseItemSource.searchByLocation(
                center, radiusInKm, keyword, categoryId, conditionId,
                minPrice, maxPrice, limit, sortField, direction
        ).addOnSuccessListener(items -> {
            callback.onSuccess(items);
            // Bạn có thể bỏ onComplete() đi nếu callback không dùng đến
            // callback.onComplete();
        }).addOnFailureListener(e -> {
            callback.onFailure(e);
            // callback.onComplete();
        });
    }



    @Override // << Giờ sẽ không báo lỗi >>
    public void incrementItemViews(String itemId) {
        firebaseItemSource.incrementItemViews(itemId)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment views for item: " + itemId, e));
    }

    @Override // << Giờ sẽ không báo lỗi >>
    public void incrementItemOffers(String itemId) {
        firebaseItemSource.incrementItemOffers(itemId)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment offers for item: " + itemId, e));
    }
}