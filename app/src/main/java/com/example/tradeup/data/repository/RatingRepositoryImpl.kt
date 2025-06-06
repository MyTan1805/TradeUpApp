package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Rating
import com.example.tradeup.data.source.remote.FirebaseRatingSource // Giả sử bạn sẽ tạo FirebaseRatingSource.kt
import javax.inject.Inject

class RatingRepositoryImpl @Inject constructor(
    private val firebaseRatingSource: FirebaseRatingSource
) : RatingRepository {

    override suspend fun submitRating(rating: Rating): Result<Unit> {
        // return firebaseRatingSource.submitRating(rating)
        TODO("Implement submitRating in FirebaseRatingSource and call it here")
    }

    override suspend fun getRatingsForUser(userId: String): Result<List<Rating>> {
        // return firebaseRatingSource.getRatingsForUser(userId)
        TODO("Implement getRatingsForUser in FirebaseRatingSource and call it here")
    }

    override suspend fun getRatingForTransaction(transactionId: String, raterId: String): Result<Rating?> {
        // return firebaseRatingSource.getRatingForTransaction(transactionId, raterId)
        TODO("Implement getRatingForTransaction in FirebaseRatingSource and call it here")
    }
    // ...
}