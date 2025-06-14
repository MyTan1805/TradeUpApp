package com.example.tradeup.data.source.remote;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.data.model.Item; // Model Item (Java)
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class FirebaseItemSource {

    private static final String TAG = "FirebaseItemSource";
    private static final String ITEMS_COLLECTION_NAME = "items"; // Đổi tên để tránh trùng với biến
    private final CollectionReference itemsCollection;

    @Inject
    public FirebaseItemSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.itemsCollection = firestore.collection(ITEMS_COLLECTION_NAME);
    }

    private final FirebaseFirestore firestore; // Giữ lại firestore instance nếu cần truy cập collection khác

    public Task<List<Item>> getItemsBySellerIdFromSource(String sellerId) {
        Log.d(TAG, "DS: Fetching items for sellerId: " + sellerId);
        return itemsCollection
                .whereEqualTo("sellerId", sellerId)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // Tùy chọn, thêm nếu cần
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "DS: Error fetching items for sellerId: " + sellerId, task.getException());
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot snapshot = task.getResult();
                    List<Item> items = snapshot.toObjects(Item.class);
                    Log.d(TAG, "DS: Fetched " + items.size() + " items for sellerId: " + sellerId);
                    return items;
                });
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
                    DocumentSnapshot documentSnapshot = task.getResult();
                    // Firestore không tự gán ID khi dùng toObject(), model Item cần có setter cho itemId
                    // Hoặc bạn phải gán thủ công sau khi lấy object nếu không dùng @DocumentId trong model Java
                    Item item = documentSnapshot.toObject(Item.class);
                    // if (item != null && documentSnapshot.exists()) {
                    //    item.setItemId(documentSnapshot.getId()); // Giả sử có setter hoặc @DocumentId
                    // }
                    return item; // Sẽ là null nếu document không tồn tại hoặc không parse được
                });
    }

    public Task<List<Item>> getAllItems(long limit, @Nullable String lastVisibleItemId) {
        Query query = itemsCollection
                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit);

        if (lastVisibleItemId != null && !lastVisibleItemId.isEmpty()) {
            // Cần lấy document cuối cùng để dùng startAfter
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
                return task.getResult().toObjects(Item.class);
            });
        } else {
            return query.get().continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return task.getResult().toObjects(Item.class);
            });
        }
    }

    public Task<List<Item>> getItemsBySeller(String sellerId, long limit, @Nullable String lastVisibleItemId) {
        Log.d(TAG, "DS: getItemsBySeller (paginated) called for sellerId: " + sellerId);
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
                return query.startAfter(lastSnapshot).get();
            }).continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return task.getResult().toObjects(Item.class);
            });
        } else {
            return query.get().continueWith(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return task.getResult().toObjects(Item.class);
            });
        }
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
}