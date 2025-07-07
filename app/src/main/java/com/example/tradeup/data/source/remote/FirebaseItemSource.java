// File: src/main/java/com/example/tradeup/data/source/remote/FirebaseItemSource.java
package com.example.tradeup.data.source.remote;

import static com.example.tradeup.ui.listing.AddressSearchDialogFragment.TAG;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.data.model.Item;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseItemSource {

    private static final String ITEMS_COLLECTION_NAME = "items";
    private final CollectionReference itemsCollection;

    @Inject
    public FirebaseItemSource(FirebaseFirestore firestore) {
        this.itemsCollection = firestore.collection(ITEMS_COLLECTION_NAME);
    }

    public Task<String> addItem(Item item) {
        return itemsCollection.add(item).continueWith(task -> {
            if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
            return Objects.requireNonNull(task.getResult()).getId();
        });
    }

    public Task<Item> getItemById(String itemId) {
        return itemsCollection.document(itemId).get()
                .continueWith(task -> task.getResult().toObject(Item.class));
    }

    public Task<List<Item>> getAllItems(long limit, @Nullable String lastVisibleItemId) {
        Query query = itemsCollection
                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit);

        Log.d(TAG, "Source: getAllItems called. Limit: " + limit + ", After: " + lastVisibleItemId);

        if (lastVisibleItemId == null || lastVisibleItemId.isEmpty()) {
            return query.get().continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Item.class));
        } else {
            return itemsCollection.document(lastVisibleItemId).get().continueWithTask(task -> {
                if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());
                return query.startAfter(task.getResult()).get();
            }).continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Item.class));
        }
    }

    public Task<List<Item>> getItemsBySellerIdFromSource(String sellerId) {
        return itemsCollection.whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING).get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Item.class));
    }

    public Task<List<Item>> searchByFilters(
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable String conditionId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            long limit,
            @NonNull String sortField, // << THÊM THAM SỐ NÀY
            @NonNull Query.Direction direction // << THÊM THAM SỐ NÀY
    ) {
        Query query = itemsCollection.whereEqualTo("status", "available");

        // Áp dụng các bộ lọc
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

        // Áp dụng sắp xếp
        // Firestore yêu cầu phải orderBy theo trường có điều kiện range filter (>, <) trước
        if (minPrice != null || maxPrice != null) {
            // Sắp xếp theo giá trước, sau đó mới đến trường sắp xếp chính nếu nó khác giá
            query = query.orderBy("price", direction); // Giả sử sắp xếp theo hướng của trường chính
            if (!sortField.equals("price")) {
                query = query.orderBy(sortField, direction);
            }
        } else {
            // Nếu không có filter giá, sắp xếp tự do
            query = query.orderBy(sortField, direction);
        }

        return query.limit(limit).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).toObjects(Item.class);
                });
    }
    public Task<List<Item>> searchByLocation(
            @NonNull Location center,
            int radiusInKm,
            @Nullable String keyword,
            @Nullable String categoryId,
            @Nullable String conditionId,
            @Nullable Double minPrice,
            @Nullable Double maxPrice,
            long limit,
            @NonNull String sortField,
            @NonNull Query.Direction direction
    ) {
        final GeoLocation geoLocation = new GeoLocation(center.getLatitude(), center.getLongitude());
        final double radiusInM = radiusInKm * 1000.0;
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(geoLocation, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : bounds) {
            Query q = itemsCollection.orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<Item> matchingItems = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (Object result : task.getResult()) {
                    if (result instanceof QuerySnapshot) {
                        matchingItems.addAll(((QuerySnapshot) result).toObjects(Item.class));
                    }
                }
            } else {
                // Ném lỗi nếu bất kỳ task nào thất bại
                throw Objects.requireNonNull(task.getException());
            }

            // --- LỌC Ở PHÍA CLIENT (LOGIC CŨ ĐÃ RẤT TỐT) ---
            List<Item> filteredItems = matchingItems.stream()
                    .filter(item -> {
                        if (item.getLocation() == null) return false;
                        double distanceInMeters = GeoFireUtils.getDistanceBetween(
                                new GeoLocation(item.getLocation().getLatitude(), item.getLocation().getLongitude()),
                                geoLocation
                        );
                        return distanceInMeters <= radiusInM;
                    })
                    .filter(item -> "available".equalsIgnoreCase(item.getStatus()))
                    .filter(item -> keyword == null || keyword.trim().isEmpty() || (item.getTitle() != null && item.getTitle().toLowerCase().contains(keyword.toLowerCase().trim())))
                    .filter(item -> categoryId == null || categoryId.isEmpty() || categoryId.equals(item.getCategory()))
                    .filter(item -> conditionId == null || conditionId.isEmpty() || conditionId.equals(item.getCondition()))
                    .filter(item -> minPrice == null || item.getPrice() >= minPrice)
                    .filter(item -> maxPrice == null || item.getPrice() <= maxPrice)
                    .collect(Collectors.toList());

            // --- SẮP XẾP Ở PHÍA CLIENT ---
            Comparator<Item> comparator;
            switch (sortField) {
                case "price":
                    comparator = Comparator.comparingDouble(Item::getPrice);
                    break;
                case "createdAt":
                default:
                    // Sắp xếp theo ngày tạo, null sẽ ở cuối
                    comparator = Comparator.comparing(Item::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                    break;
            }

            // Đảo ngược thứ tự nếu cần
            if (direction == Query.Direction.DESCENDING) {
                comparator = comparator.reversed();
            }

            filteredItems.sort(comparator);

            // --- GIỚI HẠN KẾT QUẢ CUỐI CÙNG ---
            if (filteredItems.size() > limit) {
                return filteredItems.subList(0, (int) limit);
            } else {
                return filteredItems;
            }
        });
    }
    public Task<Void> updateItem(@NonNull Item item) {
        return itemsCollection.document(item.getItemId()).set(item);
    }

    public Task<Void> deleteItem(@NonNull String itemId) {
        return itemsCollection.document(itemId).delete();
    }

    public Task<Void> updateItemStatus(String itemId, String newStatus) {
        return itemsCollection.document(itemId).update("status", newStatus);
    }

    public Task<Void> incrementItemViews(String itemId) {
        return itemsCollection.document(itemId).update("viewsCount", FieldValue.increment(1));
    }

    public Task<Void> incrementItemOffers(String itemId) {
        return itemsCollection.document(itemId).update("offersCount", FieldValue.increment(1));
    }
}