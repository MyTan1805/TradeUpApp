package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ItemLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val addressString: String = "",
    val geohash: String? = null // Optional
)

data class Item(
    @DocumentId val itemId: String = "",
    val sellerId: String = "",
    val sellerDisplayName: String = "",
    val sellerProfilePictureUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val isNegotiable: Boolean = false,
    val category: String = "", // ID from appConfig
    val subCategory: String? = null, // ID from appConfig
    val condition: String = "", // ID from appConfig
    val location: ItemLocation = ItemLocation(),
    val imageUrls: List<String> = emptyList(),
    var status: String = "available", // "available", "sold", "paused", "deleted"
    val itemBehavior: String? = null, // e.g., "pickup_only"
    val tags: List<String>? = null,
    var viewsCount: Int = 0,
    var offersCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null,
    var soldToUserId: String? = null,
    var soldAt: Timestamp? = null
)