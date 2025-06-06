package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Rating // Giả sử bạn có model Rating.kt

interface RatingRepository {
    suspend fun submitRating(rating: Rating): Result<Unit>
    suspend fun getRatingsForUser(userId: String): Result<List<Rating>>
    suspend fun getRatingForTransaction(transactionId: String, raterId: String): Result<Rating?>
}