package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Offer(
    @DocumentId val offerId: String = "",
    val itemId: String = "",
    val sellerId: String = "",
    val buyerId: String = "",
    val buyerDisplayName: String = "",
    val buyerProfilePictureUrl: String? = null,
    val offeredPrice: Double = 0.0,
    val message: String? = null,
    var status: String = "pending", // "pending", "accepted", "rejected", "countered", "cancelled_by_buyer", "expired"
    var counterOfferPrice: Double? = null,
    var counterOfferMessage: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)