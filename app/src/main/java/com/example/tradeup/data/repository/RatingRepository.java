package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Rating; // Model Rating (Java)
import java.util.List;

public interface RatingRepository {
    void submitRating(@NonNull Rating rating, Callback<Void> callback);

    void getRatingsForUser(String userId, long limit, Callback<List<Rating>> callback);

    void getRatingForTransaction(String transactionId, String raterId, Callback<Rating> callback); // Rating có thể null
}