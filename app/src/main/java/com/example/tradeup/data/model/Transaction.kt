package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Transaction(
    @DocumentId val transactionId: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemImageUrl: String? = null,
    val sellerId: String = "",
    val buyerId: String = "",
    val priceSold: Double = 0.0,
    @ServerTimestamp val transactionDate: Timestamp? = null,
    val paymentMethod: String? = null, // ID from appConfig
    var ratingGivenByBuyer: Boolean = false,
    var ratingGivenBySeller: Boolean = false
)