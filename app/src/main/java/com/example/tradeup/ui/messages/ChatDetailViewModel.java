// File: src/main/java/com/example/tradeup/ui/messages/ChatDetailViewModel.java
package com.example.tradeup.ui.messages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import android.content.Context;
import android.net.Uri;
import com.example.tradeup.core.utils.CloudinaryUploader;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class ChatDetailViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final String chatId;
    private final String currentUserId;
    private String otherUserId;

    private final Context appContext;

    private final MutableLiveData<List<Message>> _messages = new MutableLiveData<>();
    public LiveData<List<Message>> getMessages() { return _messages; }

    private final MutableLiveData<String> _otherUserAvatarUrl = new MutableLiveData<>();
    public LiveData<String> getOtherUserAvatarUrl() { return _otherUserAvatarUrl; }

    private final MutableLiveData<Boolean> _isSending = new MutableLiveData<>(false);
    public LiveData<Boolean> isSending() { return _isSending; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<String>> _navigateToUserProfileEvent = new MutableLiveData<>();
    public LiveData<Event<String>> getNavigateToUserProfileEvent() { return _navigateToUserProfileEvent; }

    private ListenerRegistration messagesListener;

    @Inject
    public ChatDetailViewModel(ChatRepository chatRepository, AuthRepository authRepository,UserRepository userRepository, SavedStateHandle savedStateHandle,
                               @ApplicationContext Context context) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.appContext = context;

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

    public void onViewProfileClicked() {
        if (otherUserId != null) {
            _navigateToUserProfileEvent.setValue(new Event<>(otherUserId));
        } else {
            _toastMessage.setValue(new Event<>("Could not load user profile."));
        }
    }

    public void onBlockUserClicked() {
        if (currentUserId == null || otherUserId == null) {
            _toastMessage.setValue(new Event<>("Error: Cannot identify users to block."));
            return;
        }

        // Gọi đến repository để thực hiện việc block
        userRepository.blockUser(currentUserId, otherUserId, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("User blocked successfully."));
                // TODO: Có thể thêm logic điều hướng người dùng ra khỏi màn hình chat này
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to block user: " + e.getMessage()));
            }
        });
    }

    public void sendImageMessage(Uri imageUri) {
        if (imageUri == null || currentUserId == null || otherUserId == null) {
            _toastMessage.postValue(new Event<>("Error: Cannot send image."));
            return;
        }

        _isSending.setValue(true);
        _toastMessage.postValue(new Event<>("Uploading image..."));

        // Tải ảnh lên Cloudinary
        CloudinaryUploader.uploadImageDirectlyToCloudinary(appContext, imageUri, new CloudinaryUploader.CloudinaryUploadCallback() {
            @Override
            public void onSuccess(@NonNull String imageUrl) {
                // Sau khi có URL, tạo message và gửi đi
                Message imageMessage = new Message();
                imageMessage.setImageUrl(imageUrl);
                imageMessage.setSenderId(currentUserId);
                imageMessage.setReceiverId(otherUserId);
                imageMessage.setType("image");

                chatRepository.sendMessage(chatId, imageMessage, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        _isSending.postValue(false);
                    }
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        _isSending.postValue(false);
                        _toastMessage.postValue(new Event<>("Failed to send image message."));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isSending.postValue(false);
                _toastMessage.postValue(new Event<>("Image upload failed: " + e.getMessage()));
            }

            @Override
            public void onErrorResponse(int code, @Nullable String errorMessage) {
                _isSending.postValue(false);
                _toastMessage.postValue(new Event<>("Image upload failed. Error " + code));
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