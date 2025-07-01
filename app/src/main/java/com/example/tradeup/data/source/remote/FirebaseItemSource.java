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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class FirebaseItemSource {

    private static final String TAG = "FirebaseItemSource";
    private static final String ITEMS_COLLECTION_NAME = "items";
    private final CollectionReference itemsCollection;
    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseItemSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.itemsCollection = firestore.collection(ITEMS_COLLECTION_NAME);
    }

    public Task<String> addItem(Item item) {
        return itemsCollection.add(item)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).getId();
                });
    }

    public Task<Item> getItemById(String itemId) {
        return itemsCollection.document(itemId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toObject(Item.class);
                });
    }

    public Task<List<Item>> getAllItems(long limit, @Nullable String lastVisibleItemId) {
        Query query = itemsCollection
                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit);

        if (lastVisibleItemId != null && !lastVisibleItemId.isEmpty()) {
            return itemsCollection.document(lastVisibleItemId).get().continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                DocumentSnapshot lastSnapshot = task.getResult();
                return query.startAfter(lastSnapshot).get();
            }).continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
            });
        } else {
            return query.get().continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
            });
        }
    }

    public Task<List<Item>> getItemsBySellerIdFromSource(String sellerId) {
        return itemsCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toObjects(Item.class);
                });
    }

    public Task<List<Item>> getItemsBySeller(String sellerId, long limit, @Nullable String lastVisibleItemId) {
        Query query = itemsCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit);

        if (lastVisibleItemId != null && !lastVisibleItemId.isEmpty()) {
            return itemsCollection.document(lastVisibleItemId).get().continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                DocumentSnapshot lastSnapshot = task.getResult();
                if (lastSnapshot.exists()) {
                    return query.startAfter(lastSnapshot).get();
                } else {
                    return query.get();
                }
            }).continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
            });
        } else {
            return query.get().continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
            });
        }
    }

    public Task<List<Item>> searchItemsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Tasks.forResult(Collections.emptyList());
        }
        return itemsCollection
                .whereArrayContains("searchKeywords", keyword.toLowerCase().trim())
                .whereEqualTo("status", "available")
                .limit(30)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
                });
    }

    public Task<Void> updateItem(Item item) {
        if (item.getItemId() == null || item.getItemId().trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Item ID cannot be blank for update"));
        }
        return itemsCollection.document(item.getItemId()).set(item);
    }

    public Task<Void> deleteItem(String itemId) {
        return itemsCollection.document(itemId).delete();
    }

    public Task<Void> updateItemStatus(String itemId, String newStatus) {
        return itemsCollection.document(itemId).update("status", newStatus);
    }

    public Task<List<Item>> searchItems(
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            @Nullable String condition
    )
    {
        // Bắt đầu với câu query cơ bản
        Query query = itemsCollection.whereEqualTo("status", "available");

        // 1. Áp dụng bộ lọc từ khóa (nếu có)
        if (keyword != null && !keyword.trim().isEmpty()) {
            query = query.whereArrayContains("searchKeywords", keyword.toLowerCase().trim());
        }

        // 2. Áp dụng bộ lọc danh mục (nếu có)
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("category", categoryId);
        }

        // 3. Áp dụng bộ lọc tình trạng (nếu có)
        if (condition != null && !condition.isEmpty()) {
            query = query.whereEqualTo("condition", condition);
        }

        // 4. Áp dụng bộ lọc giá
        if (minPrice != null) {
            query = query.whereGreaterThanOrEqualTo("price", minPrice);
        }
        if (maxPrice != null) {
            query = query.whereLessThanOrEqualTo("price", maxPrice);
        }

        // Sắp xếp và giới hạn kết quả
        // Nếu có whereArrayContains, Firestore không cho phép orderBy trên trường khác
        if (keyword == null || keyword.trim().isEmpty()) {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }
        query = query.limit(20);

        return query.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Search failed: ", task.getException());
                throw Objects.requireNonNull(task.getException());
            }
            return task.getResult().toObjects(Item.class);
        });
    }
}