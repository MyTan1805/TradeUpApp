package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Rating(
    @DocumentId val ratingId: String = "",
    val transactionId: String = "",
    val itemId: String = "",
    val ratedUserId: String = "",
    val raterUserId: String = "",
    val raterDisplayName: String = "",
    val raterProfilePictureUrl: String? = null,
    val stars: Int = 0, // 1-5
    val feedbackText: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)