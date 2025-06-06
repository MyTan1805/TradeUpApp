package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserSavedItems(
    @DocumentId val userId: String = "", // Corresponds to the user's UID
    val itemIds: List<String> = emptyList(),
    @ServerTimestamp var updatedAt: Timestamp? = null
)