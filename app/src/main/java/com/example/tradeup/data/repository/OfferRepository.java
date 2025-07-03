// File: src/main/java/com/example/tradeup/data/repository/OfferRepository.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Offer;
import java.util.List;
import java.util.Map;

// File: src/main/java/com/example/tradeup/data/repository/OfferRepository.java
public interface OfferRepository {
    void createOffer(@NonNull Offer offer, Callback<String> callback);
    void getOfferById(String offerId, Callback<Offer> callback);
    void getOffersForItem(String itemId, Callback<List<Offer>> callback);
    void getOffersByBuyer(String buyerId, Callback<List<Offer>> callback);
    void getOffersForSeller(String sellerId, Callback<List<Offer>> callback);
    void updateOffer(@NonNull String offerId, @NonNull String newStatus,
                     @Nullable Double newPrice, @Nullable String newMessage, Callback<Void> callback);
    void updateOffer(@NonNull String offerId, @NonNull Map<String, Object> updates, Callback<Void> callback);
}