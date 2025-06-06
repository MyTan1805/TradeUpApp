package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class ContactInfo(
    val phone: String? = null,
    val zalo: String? = null,
    val facebook: String? = null
)

data class User(
    @DocumentId val uid: String = "", // Should match Firebase Auth UID
    val email: String = "",
    var displayName: String = "",
    var profilePictureUrl: String? = null,
    var bio: String? = null,
    var contactInfo: ContactInfo? = null,
    val averageRating: Double = 0.0,
    val totalTransactions: Int = 0,
    val totalListings: Int = 0,
    val fcmTokens: List<String>? = null, // List of FCM tokens
    var location: GeoPoint? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null,
    val isDeactivated: Boolean = false,
    var lastLoginAt: Timestamp? = null
)