package com.example.tradeup.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Report(
    @DocumentId val reportId: String = "",
    val reportingUserId: String = "",
    val reportedContentType: String = "", // "listing", "profile", "chatMessage"
    val reportedContentId: String = "",
    val reportedUserId: String? = null, // UID of the owner of the reported content
    val reason: String = "", // ID from appConfig
    val details: String? = null,
    var status: String = "pending_review", // "pending_review", "under_review", "resolved_action_taken", "resolved_no_action"
    @ServerTimestamp val createdAt: Timestamp? = null,
    val adminNotes: String? = null, // Client should not write
    var resolvedAt: Timestamp? = null // Client should not write
)