package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Chat
import com.example.tradeup.data.model.Message
import com.example.tradeup.data.source.remote.FirebaseChatSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firebaseChatSource: FirebaseChatSource
) : ChatRepository {

    override suspend fun getOrCreateChat(participantIds: List<String>, relatedItemId: String?): Result<String> {
        return firebaseChatSource.getOrCreateChat(participantIds, relatedItemId)
    }

    override fun getChatList(userId: String): Flow<Result<List<Chat>>> {
        return firebaseChatSource.getChatList(userId)
    }

    override fun getMessages(chatId: String): Flow<Result<List<Message>>> {
        return firebaseChatSource.getMessages(chatId)
    }

    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return firebaseChatSource.sendMessage(chatId, message)
    }

    override suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        return firebaseChatSource.markMessagesAsRead(chatId, userId)
    }
}