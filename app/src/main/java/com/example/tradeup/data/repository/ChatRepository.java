package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Cho relatedItemId

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Chat; // Model Chat (Java)
import com.example.tradeup.data.model.Message; // Model Message (Java)
import com.example.tradeup.data.model.User;
import com.google.firebase.firestore.ListenerRegistration; // Để quản lý listener

import java.util.List;

public interface ChatRepository {
    void getOrCreateChat(
            String currentUserId, User currentUserInfo,
            String otherUserId, User otherUserInfo,
            @Nullable String relatedItemId, Callback<String> callback
    );

    // Để lắng nghe real-time, Repository sẽ trả về ListenerRegistration để ViewModel có thể hủy đăng ký khi không cần nữa
    ListenerRegistration getChatList(String userId, Callback<List<Chat>> callback);

    ListenerRegistration getMessages(String chatId, Callback<List<Message>> callback);

    void sendMessage(String chatId, Message message, Callback<Void> callback);

    void markMessagesAsRead(String chatId, String userId, Callback<Void> callback);
}