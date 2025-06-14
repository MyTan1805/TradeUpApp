package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.Message;
import com.example.tradeup.data.source.remote.FirebaseChatSource;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatRepositoryImpl implements ChatRepository {

    private final FirebaseChatSource firebaseChatSource;

    @Inject
    public ChatRepositoryImpl(FirebaseChatSource firebaseChatSource) {
        this.firebaseChatSource = firebaseChatSource;
    }

    @Override
    public void getOrCreateChat(List<String> participantIds, @Nullable String relatedItemId, Callback<String> callback) {
        firebaseChatSource.getOrCreateChat(participantIds, relatedItemId, callback);
    }

    @Override
    public ListenerRegistration getChatList(String userId, Callback<List<Chat>> callback) {
        return firebaseChatSource.getChatList(userId, callback);
    }

    @Override
    public ListenerRegistration getMessages(String chatId, Callback<List<Message>> callback) {
        return firebaseChatSource.getMessages(chatId, callback);
    }

    @Override
    public void sendMessage(String chatId, Message message, Callback<Void> callback) {
        firebaseChatSource.sendMessage(chatId, message, callback);
    }

    @Override
    public void markMessagesAsRead(String chatId, String userId, Callback<Void> callback) {
        firebaseChatSource.markMessagesAsRead(chatId, userId, callback);
    }
}