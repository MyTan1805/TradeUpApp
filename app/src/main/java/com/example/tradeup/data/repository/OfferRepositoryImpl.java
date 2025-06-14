package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.data.source.remote.FirebaseOfferSource;

import java.util.List;
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
                .addOnSuccessListener(offer -> {
                    // offer có thể là null nếu không tìm thấy
                    callback.onSuccess(offer);
                })
                .addOnFailureListener(callback::onFailure);
    }

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
    public void updateOfferStatus(String offerId, @NonNull String newStatus,
                                  @Nullable Double counterPrice, @Nullable String counterMessage,
                                  Callback<Void> callback) {
        firebaseOfferSource.updateOfferStatus(offerId, newStatus, counterPrice, counterMessage)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}
