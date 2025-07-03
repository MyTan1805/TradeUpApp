// File: src/main/java/com/example/tradeup/data/repository/OfferRepositoryImpl.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.data.source.remote.FirebaseOfferSource;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OfferRepositoryImpl implements OfferRepository {

    private final FirebaseOfferSource firebaseOfferSource;

    @Inject
    public OfferRepositoryImpl(FirebaseOfferSource firebaseOfferSource) {
        this.firebaseOfferSource = firebaseOfferSource;
    }

    @Override
    public void createOffer(@NonNull Offer offer, Callback<String> callback) {
        firebaseOfferSource.createOffer(offer)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getOfferById(String offerId, Callback<Offer> callback) {
        firebaseOfferSource.getOfferById(offerId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    // <<< TRIỂN KHAI PHƯƠNG THỨC BỊ THIẾU >>>
    @Override
    public void getOffersForItem(String itemId, Callback<List<Offer>> callback) {
        firebaseOfferSource.getOffersForItem(itemId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getOffersByBuyer(String buyerId, Callback<List<Offer>> callback) {
        firebaseOfferSource.getOffersByBuyer(buyerId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getOffersForSeller(String sellerId, Callback<List<Offer>> callback) {
        firebaseOfferSource.getOffersForSeller(sellerId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateOffer(@NonNull String offerId, @NonNull String newStatus,
                            @Nullable Double newPrice, @Nullable String newMessage, Callback<Void> callback) {
        firebaseOfferSource.updateOffer(offerId, newStatus, newPrice, newMessage)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateOffer(@NonNull String offerId, @NonNull Map<String, Object> updates, Callback<Void> callback) {
        firebaseOfferSource.updateOffer(offerId, updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}