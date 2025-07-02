package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.data.model.Offer; // Model Offer (Java)
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

public class FirebaseOfferSource {

    private final CollectionReference offersCollection;

    @Inject
    public FirebaseOfferSource(FirebaseFirestore firestore) {
        this.offersCollection = firestore.collection("offers");
    }

    public Task<String> createOffer(@NonNull Offer offer) {
        // Firestore sẽ tự xử lý @ServerTimestamp trong POJO Offer khi thêm mới
        return offersCollection.add(offer)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).getId();
                });
    }

    public Task<Offer> getOfferById(String offerId) {
        return offersCollection.document(offerId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    DocumentSnapshot documentSnapshot = task.getResult();
                    return documentSnapshot.toObject(Offer.class); // Sẽ là null nếu không tồn tại
                });
    }

    public Task<List<Offer>> getOffersForItem(String itemId) {
        return offersCollection
                .whereEqualTo("itemId", itemId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toObjects(Offer.class);
                });
    }

    public Task<List<Offer>> getOffersByBuyer(String buyerId) {
        return offersCollection
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toObjects(Offer.class);
                });
    }

    public Task<List<Offer>> getOffersForSeller(String sellerId) {
        return offersCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toObjects(Offer.class);
                });
    }


    public Task<Void> updateOfferStatus(
            @NonNull String offerId,
            @NonNull String newStatus,
            @Nullable Double counterPrice,
            @Nullable String counterMessage) {

        DocumentReference offerRef = offersCollection.document(offerId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", FieldValue.serverTimestamp()); // Luôn cập nhật thời gian

        // Logic riêng cho "countered"
        if ("countered".equals(newStatus)) {
            if (counterPrice == null || counterPrice <= 0) {
                return Tasks.forException(new IllegalArgumentException("Counter price must be positive."));
            }
            updates.put("counterOfferPrice", counterPrice);
            if (counterMessage != null) {
                updates.put("counterOfferMessage", counterMessage);
            }
        }
        // Logic khi chấp nhận/từ chối: xóa các trường counter offer cũ (nếu có)
        else if ("accepted".equals(newStatus) || "rejected".equals(newStatus)) {
            updates.put("counterOfferPrice", FieldValue.delete());
            updates.put("counterOfferMessage", FieldValue.delete());
        }

        return offerRef.update(updates);
    }
}