package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Rating;
import com.example.tradeup.data.source.remote.FirebaseRatingSource;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RatingRepositoryImpl implements RatingRepository {

    private final FirebaseRatingSource firebaseRatingSource;

    @Inject
    public RatingRepositoryImpl(FirebaseRatingSource firebaseRatingSource) {
        this.firebaseRatingSource = firebaseRatingSource;
    }

    @Override
    public void submitRating(@NonNull Rating rating, final Callback<Void> callback) {
        firebaseRatingSource.submitRating(rating)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getRatingsForUser(String userId, long limit, final Callback<List<Rating>> callback) {
        firebaseRatingSource.getRatingsForUser(userId, limit)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getRatingForTransaction(String transactionId, String raterId, final Callback<Rating> callback) {
        firebaseRatingSource.getRatingForTransaction(transactionId, raterId)
                .addOnSuccessListener(callback::onSuccess) // Rating có thể là null
                .addOnFailureListener(callback::onFailure);
    }
}