package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ParticipantInfoDetail(
    val displayName: String = "",
    val profilePictureUrl: String? = null
)

data class Chat(
    @DocumentId val chatId: String = "",
    val participants: List<String> = emptyList(), // List of 2 userIds
    val participantInfo: Map<String, ParticipantInfoDetail> = emptyMap(), // Key: userId
    var lastMessageText: String? = null,
    @ServerTimestamp var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String? = null,
    val unreadCount: Map<String, Int> = emptyMap(), // Key: userId, Value: count
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null,
    val relatedItemId: String? = null
)

// Subcollection: messages
data class OfferNotificationDetails( // For message type "offer_notification"
    val offerId: String = "",
    val offeredPrice: Double = 0.0,
    val status: String = "" // "pending", "accepted", "rejected", "countered"
)

data class Message(
    @DocumentId val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "", // Useful for querying/filtering, though chatId already implies participants
    val text: String? = null,
    val imageUrl: String? = null,
    val type: String = "text", // "text", "image", "offer_notification"
    @ServerTimestamp val timestamp: Timestamp? = null,
    var isRead: Boolean = false, // Primarily for the receiver
    val offerDetails: OfferNotificationDetails? = null // Only if type is "offer_notification"
)