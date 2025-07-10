// File: src/main/java/com/example/tradeup/ui/messages/ChatDetailViewModel.java
package com.example.tradeup.ui.messages;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.Message;
import com.example.tradeup.data.model.ParticipantInfoDetail;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

import com.example.tradeup.data.model.User; // << THÊM IMPORT NÀY
import com.example.tradeup.data.repository.UserRepository;

@HiltViewModel
public class ChatDetailViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final String chatId;
    private final String currentUserId;
    private String otherUserId;

    private final MutableLiveData<List<Message>> _messages = new MutableLiveData<>();
    public LiveData<List<Message>> getMessages() { return _messages; }

    private final MutableLiveData<String> _otherUserAvatarUrl = new MutableLiveData<>();
    public LiveData<String> getOtherUserAvatarUrl() { return _otherUserAvatarUrl; }

    private final MutableLiveData<Boolean> _isSending = new MutableLiveData<>(false);
    public LiveData<Boolean> isSending() { return _isSending; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private ListenerRegistration messagesListener;

    @Inject
    public ChatDetailViewModel(ChatRepository chatRepository, AuthRepository authRepository,UserRepository userRepository, SavedStateHandle savedStateHandle) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;

        // Lấy chatId từ NavArgs
        this.chatId = savedStateHandle.get("chatId");
        FirebaseUser currentUser = authRepository.getCurrentUser();
        this.currentUserId = (currentUser != null) ? currentUser.getUid() : null;

        if (this.chatId != null && this.currentUserId != null) {
            findOtherUserId(); // << CHỈ CẦN GỌI HÀM NÀY
            listenForMessages();
        }
    }
    private void findOtherUserId() {
        // Tách ID từ chatId. Ví dụ: "id1_id2" -> ["id1", "id2"]
        String[] ids = chatId.split("_");
        if (ids.length == 2) {
            this.otherUserId = ids[0].equals(currentUserId) ? ids[1] : ids[0];
            fetchOtherUserInfo(); // Gọi hàm fetch sau khi có otherUserId
        }
    }
    private void listenForMessages() {
        messagesListener = chatRepository.getMessages(chatId, new Callback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                _messages.postValue(data);
                // Đánh dấu đã đọc tin nhắn
                chatRepository.markMessagesAsRead(chatId, currentUserId, new Callback<Void>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onFailure(@NonNull Exception e) {}
                });
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Error loading messages: " + e.getMessage()));
            }
        });
    }

    // Hàm này bị thiếu, cần được thêm vào
    private void fetchOtherUserInfo() {
        if (otherUserId == null) return;
        userRepository.getUserProfile(otherUserId, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    _otherUserAvatarUrl.postValue(user.getProfilePictureUrl());
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                // Không làm gì, sẽ dùng ảnh placeholder
            }
        });
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty() || currentUserId == null || otherUserId == null) { // << KIỂM TRA otherUserId
            return;
        }
        _isSending.setValue(true);

        Message newMessage = new Message();
        newMessage.setText(text.trim());
        newMessage.setSenderId(currentUserId);
        newMessage.setReceiverId(otherUserId); // << GÁN RECEIVER ID
        newMessage.setType("text");

        chatRepository.sendMessage(chatId, newMessage, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isSending.postValue(false);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isSending.postValue(false);
                _toastMessage.postValue(new Event<>("Failed to send message."));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}