// File: src/main/java/com/example/tradeup/data/source/remote/FirebaseOfferSource.java
package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.data.model.Offer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
        return offersCollection.add(offer)
                .continueWith(task -> Objects.requireNonNull(task.getResult()).getId());
    }

    public Task<Offer> getOfferById(String offerId) {
        return offersCollection.document(offerId).get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObject(Offer.class));
    }

    public Task<List<Offer>> getOffersForItem(String itemId) {
        return offersCollection
                .whereEqualTo("itemId", itemId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Offer.class));
    }

    public Task<List<Offer>> getOffersByBuyer(String buyerId) {
        return offersCollection
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Offer.class));
    }

    public Task<List<Offer>> getOffersForSeller(String sellerId) {
        return offersCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> Objects.requireNonNull(task.getResult()).toObjects(Offer.class));
    }

    public Task<Void> updateOffer(
            @NonNull String offerId, @NonNull String newStatus,
            @Nullable Double newPrice, @Nullable String newMessage
    ) {
        DocumentReference offerRef = offersCollection.document(offerId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", FieldValue.serverTimestamp());
        if (newPrice != null) updates.put("offeredPrice", newPrice); // Cập nhật offeredPrice thay vì currentPrice
        if (newMessage != null) updates.put("message", newMessage);
        return offerRef.update(updates);
    }
    public Task<Void> updateOffer(@NonNull String offerId, @NonNull Map<String, Object> updates) {
        DocumentReference offerRef = offersCollection.document(offerId);
        return offerRef.update(updates);
    }
}