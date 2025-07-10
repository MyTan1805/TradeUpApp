// File: src/main/java/com/example/tradeup/ui/messages/ChatViewModel.java
package com.example.tradeup.ui.messages;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends ViewModel {
    private static final String TAG = "ChatViewModel";

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<ChatState> _chatState = new MutableLiveData<>();
    public LiveData<ChatState> getChatState() {
        return _chatState;
    }

    private ListenerRegistration chatListenerRegistration;

    @Inject
    public ChatViewModel(ChatRepository chatRepository, AuthRepository authRepository) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        listenForChats();
    }

    private void listenForChats() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _chatState.setValue(new ChatState.Error("Please log in to see your messages."));
            return;
        }

        _chatState.setValue(new ChatState.Loading());

        // Bắt đầu lắng nghe real-time
        chatListenerRegistration = chatRepository.getChatList(currentUser.getUid(), new Callback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> chats) {
                if (chats == null || chats.isEmpty()) {
                    _chatState.postValue(new ChatState.Empty());
                } else {
                    _chatState.postValue(new ChatState.Success(chats));
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error listening for chats", e);
                _chatState.postValue(new ChatState.Error("Failed to load conversations."));
            }
        });
    }

    // Được gọi khi ViewModel bị hủy (khi Fragment bị destroy)
    @Override
    protected void onCleared() {
        super.onCleared();
        // Rất quan trọng: Hủy đăng ký listener để tránh rò rỉ bộ nhớ
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
    }
}