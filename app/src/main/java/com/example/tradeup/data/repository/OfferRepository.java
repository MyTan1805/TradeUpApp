package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Offer; // Model Offer (Java)

import java.util.List;

public interface OfferRepository {
    void createOffer(@NonNull Offer offer, Callback<String> callback); // Trả về offerId

    void getOfferById(String offerId, Callback<Offer> callback); // Offer có thể null

    void getOffersForItem(String itemId, Callback<List<Offer>> callback);

    void getOffersByBuyer(String buyerId, Callback<List<Offer>> callback);

    void getOffersForSeller(String sellerId, Callback<List<Offer>> callback); // Thêm hàm này

    void updateOfferStatus(String offerId, @NonNull String newStatus,
                           @Nullable Double counterPrice, @Nullable String counterMessage,
                           Callback<Void> callback);
}