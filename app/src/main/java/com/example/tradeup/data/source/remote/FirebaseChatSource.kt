package com.example.tradeup.data.source.remote // Đã đổi

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.example.tradeup.data.model.Chat // Đã đổi
import com.example.tradeup.data.model.Message // Đã đổi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ExperimentalCoroutinesApi
class FirebaseChatSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val chatsCollection = firestore.collection("chats")

    suspend fun getOrCreateChat(participantIds: List<String>, relatedItemId: String? = null): Result<String> {
        if (participantIds.size != 2) return Result.failure(IllegalArgumentException("Chat must have 2 participants"))
        val sortedIds = participantIds.sorted()
        val chatId = "${sortedIds[0]}_${sortedIds[1]}"

        return try {
            val chatDoc = chatsCollection.document(chatId)
            val snapshot = chatDoc.get().await()
            if (snapshot.exists()) {
                Result.success(chatId)
            } else {
                val newChat = Chat(
                    chatId = chatId,
                    participants = sortedIds,
                    relatedItemId = relatedItemId
                )
                chatDoc.set(newChat).await()
                Result.success(chatId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChatList(userId: String): Flow<Result<List<Chat>>> {
        return chatsCollection
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { querySnapshot ->
                try {
                    val chats = querySnapshot.documents.mapNotNull { it.toObject<Chat>() }
                    Result.success(chats)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }

    fun getMessages(chatId: String): Flow<Result<List<Message>>> {
        return chatsCollection.document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { querySnapshot ->
                try {
                    val messages = querySnapshot.documents.mapNotNull { it.toObject<Message>() }
                    Result.success(messages)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }

    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            chatsCollection.document(chatId).collection("messages").add(message).await()
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessageText" to (message.text ?: (if(message.imageUrl != null) "Image" else "Offer")),
                    "lastMessageTimestamp" to message.timestamp,
                    "lastMessageSenderId" to message.senderId,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        return try {
            chatsCollection.document(chatId).update("unreadCount.$userId", 0).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}