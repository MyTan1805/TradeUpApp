// File: src/main/java/com/example/tradeup/data/source/remote/FirebaseItemSource.java
package com.example.tradeup.data.source.remote;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.data.model.Item;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseItemSource {

    private static final String TAG = "FirebaseItemSource";
    private static final String ITEMS_COLLECTION_NAME = "items";

    private final FirebaseFirestore firestore;
    private final CollectionReference itemsCollection;

    @Inject
    public FirebaseItemSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.itemsCollection = firestore.collection(ITEMS_COLLECTION_NAME);
    }

    public Task<String> addItem(Item item) {
        // add() tự tạo ID và trả về một DocumentReference
        return itemsCollection.add(item).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return Objects.requireNonNull(task.getResult()).getId();
        });
    }

    public Task<Item> getItemById(String itemId) {
        return itemsCollection.document(itemId).get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            DocumentSnapshot snapshot = task.getResult();
            return snapshot.exists() ? snapshot.toObject(Item.class) : null;
        });
    }

    public Task<List<Item>> getAllItems(long limit, @Nullable String lastVisibleItemId) {
        Query query = itemsCollection
                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit);

        if (lastVisibleItemId == null || lastVisibleItemId.isEmpty()) {
            // Lấy trang đầu tiên
            return query.get().continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Item.class));
        } else {
            // Lấy các trang tiếp theo
            return itemsCollection.document(lastVisibleItemId).get().continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                DocumentSnapshot lastSnapshot = task.getResult();
                return query.startAfter(lastSnapshot).get();
            }).continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Item.class));
        }
    }

    public Task<List<Item>> getItemsBySellerIdFromSource(String sellerId) {
        return itemsCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Item.class));
    }

    /**
     * Hàm tìm kiếm item dựa trên các tiêu chí lọc.
     * Đã loại bỏ logic tìm kiếm theo vị trí.
     */
    public Task<List<Item>> searchItems(
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable String conditionId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            long limit
    ) {
        Query query = itemsCollection.whereEqualTo("status", "available");

        if (keyword != null && !keyword.trim().isEmpty()) {
            query = query.whereArrayContains("searchKeywords", keyword.toLowerCase().trim());
        }
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("category", categoryId);
        }
        if (conditionId != null && !conditionId.isEmpty()) {
            query = query.whereEqualTo("condition", conditionId);
        }
        if (minPrice != null) {
            query = query.whereGreaterThanOrEqualTo("price", minPrice);
        }
        if (maxPrice != null) {
            query = query.whereLessThanOrEqualTo("price", maxPrice);
        }

        // Chỉ có thể sắp xếp nếu không dùng filter `whereArrayContains`.
        if (keyword == null || keyword.trim().isEmpty()) {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        query = query.limit(limit);

        return query.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Search failed: ", task.getException());
                throw Objects.requireNonNull(task.getException());
            }
            return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
        });
    }

    public Task<Void> updateItem(@NonNull Item item) {
        if (item.getItemId() == null || item.getItemId().trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Item ID cannot be blank for update"));
        }
        return itemsCollection.document(item.getItemId()).set(item);
    }

    public Task<Void> deleteItem(@NonNull String itemId) {
        if (itemId.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Item ID cannot be blank for delete"));
        }
        return itemsCollection.document(itemId).delete();
    }

    public Task<Void> updateItemStatus(String itemId, String newStatus) {
        return itemsCollection.document(itemId).update("status", newStatus);
    }

    public Task<Void> incrementItemViews(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Item ID cannot be null for incrementing views."));
        }
        // Sử dụng FieldValue.increment để tăng giá trị một cách an toàn, tránh race condition
        return itemsCollection.document(itemId).update("viewsCount", FieldValue.increment(1));
    }

    public Task<Void> incrementItemOffers(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Item ID cannot be null for incrementing offers."));
        }
        return itemsCollection.document(itemId).update("offersCount", FieldValue.increment(1));
    }
}