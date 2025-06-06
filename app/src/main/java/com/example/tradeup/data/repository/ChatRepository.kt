package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Chat
import com.example.tradeup.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getOrCreateChat(participantIds: List<String>, relatedItemId: String? = null): Result<String> // Trả về chatId
    fun getChatList(userId: String): Flow<Result<List<Chat>>>
    fun getMessages(chatId: String): Flow<Result<List<Message>>>
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit>
    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit>
}