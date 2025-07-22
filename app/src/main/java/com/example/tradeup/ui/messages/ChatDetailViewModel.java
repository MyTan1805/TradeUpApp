// File: src/main/java/com/example/tradeup/ui/messages/ChatDetailViewModel.java
package com.example.tradeup.ui.messages;

import static com.example.tradeup.ui.offers.CounterOfferDialogFragment.TAG;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Message;
import com.example.tradeup.data.model.Notification;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.network.NotificationApiService;
import com.example.tradeup.data.network.NotificationRequest;
import com.example.tradeup.data.network.NotificationResponse;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.example.tradeup.data.repository.NotificationRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Response;

@HiltViewModel
public class ChatDetailViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;
    private final NotificationApiService notificationApiService;


    private String currentUserName; // << THÊM: Lưu tên người dùng hiện tại
    private String otherUserName;   // << THÊM: Lưu tên người đối diện

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final Context appContext;
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

    private final MutableLiveData<Event<String>> _navigateToUserProfileEvent = new MutableLiveData<>();
    public LiveData<Event<String>> getNavigateToUserProfileEvent() { return _navigateToUserProfileEvent; }

    private ListenerRegistration messagesListener;

    @Inject
    public ChatDetailViewModel(ChatRepository chatRepository, AuthRepository authRepository,
                               UserRepository userRepository, SavedStateHandle savedStateHandle,
                               @ApplicationContext Context context,NotificationRepository notificationRepository,
                               NotificationApiService notificationApiService) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.notificationApiService = notificationApiService;

        this.otherUserName = savedStateHandle.get("otherUserName");

        this.appContext = context;
        this.chatId = savedStateHandle.get("chatId");
        FirebaseUser currentUser = authRepository.getCurrentUser();
        this.currentUserId = (currentUser != null) ? currentUser.getUid() : null;

        if (this.chatId != null && this.currentUserId != null) {
            findOtherUserId();
            listenForMessages();
        }
        fetchCurrentUserInfo();

    }

    private void fetchCurrentUserInfo() {
        if (currentUserId != null) {
            userRepository.getUserProfile(currentUserId)
                    .whenComplete((user, throwable) -> {
                        if (user != null) {
                            this.currentUserName = user.getDisplayName();
                        }
                    });
        }
    }

    private void findOtherUserId() {
        String[] ids = chatId.split("_");
        if (ids.length == 2) {
            this.otherUserId = ids[0].equals(currentUserId) ? ids[1] : ids[0];
            fetchOtherUserInfo();
        }
    }

    // listenForMessages vẫn dùng Callback vì nó là một listener real-time, không phải tác vụ một lần
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

    private void fetchOtherUserInfo() {
        if (otherUserId == null) return;
        userRepository.getUserProfile(otherUserId)
                .whenComplete((user, throwable) -> {
                    if (user != null) {
                        _otherUserAvatarUrl.postValue(user.getProfilePictureUrl());
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

        userRepository.blockUser(currentUserId, otherUserId)
                .whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to block user: " + throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("User blocked successfully."));
                    }
                });
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty() || currentUserId == null || otherUserId == null) {
            return;
        }
        _isSending.setValue(true);

        Message newMessage = new Message();
        newMessage.setText(text.trim());
        newMessage.setSenderId(currentUserId);
        newMessage.setReceiverId(otherUserId);
        newMessage.setType("text");

        sendMessageAsync(newMessage)
                .whenComplete((aVoid, throwable) -> {
                    _isSending.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to send message."));
                    } else {
                            sendNewMessageNotification(newMessage);
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

        uploadImageAsync(imageUri)
                .thenCompose(imageUrl -> {
                    Message imageMessage = new Message();
                    imageMessage.setImageUrl(imageUrl);
                    imageMessage.setSenderId(currentUserId);
                    imageMessage.setReceiverId(otherUserId);
                    imageMessage.setType("image");
                    sendNewMessageNotification(imageMessage);
                    return sendMessageAsync(imageMessage);
                })
                .whenComplete((aVoid, throwable) -> {
                    _isSending.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to send image: " + throwable.getMessage()));
                    }
                });
    }

    private void sendNewMessageNotification(Message message) {
        if (currentUserName == null) {
            // Không gửi thông báo nếu chưa lấy được tên người gửi
            Log.w(TAG, "Cannot send notification, sender name is not loaded yet.");
            return;
        }

        // 1. Tạo và lưu notification vào Firestore (để hiển thị trong tab Notifications)
        Notification notif = new Notification();
        notif.setUserId(message.getReceiverId()); // Người nhận thông báo
        notif.setType("new_message");
        notif.setTitle(currentUserName); // Tiêu đề là tên người gửi
        notif.setMessage("image".equals(message.getType()) ? "Sent you an image" : message.getText());
        notif.setRelatedContentId(chatId); // ID để mở đúng chat
        notif.setRead(false);

        notificationRepository.createNotification(notif, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "New message notification stored in Firestore.");
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to store new message notification.", e);
            }
        });

        // 2. Gửi Push Notification qua FCM
        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("type", "new_message");
        dataPayload.put("chatId", chatId);
        dataPayload.put("senderId", message.getSenderId());
        dataPayload.put("senderName", currentUserName); // Gửi kèm tên người gửi

        NotificationRequest request = new NotificationRequest(
                message.getReceiverId(),
                currentUserName, // Title
                "image".equals(message.getType()) ? "📷 Photo" : message.getText(), // Body
                dataPayload
        );

        notificationApiService.sendNotification(request).enqueue(new retrofit2.Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Log.d(TAG, "Push notification for new message sent successfully.");
                } else {
                    Log.e(TAG, "Failed to send push notification for new message.");
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Log.e(TAG, "Error sending push notification for new message: " + t.getMessage());
            }
        });
    }


    // --- CÁC HÀM TIỆN ÍCH BẤT ĐỒNG BỘ ---
    private CompletableFuture<String> uploadImageAsync(Uri imageUri) {
        CompletableFuture<String> future = new CompletableFuture<>();
        CloudinaryUploader.uploadImageDirectlyToCloudinary(appContext, imageUri, new CloudinaryUploader.CloudinaryUploadCallback() {
            @Override
            public void onSuccess(@NonNull String imageUrl) { future.complete(imageUrl); }
            @Override
            public void onFailure(@NonNull Exception e) { future.completeExceptionally(e); }
            @Override
            public void onErrorResponse(int code, @Nullable String errorMessage) {
                future.completeExceptionally(new Exception("Image upload failed. Error " + code));
            }
        });
        return future;
    }

    private CompletableFuture<Void> sendMessageAsync(Message message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        chatRepository.sendMessage(chatId, message, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) { future.complete(null); }
            @Override
            public void onFailure(@NonNull Exception e) { future.completeExceptionally(e); }
        });
        return future;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}