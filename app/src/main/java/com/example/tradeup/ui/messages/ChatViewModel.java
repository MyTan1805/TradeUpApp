// File: src/main/java/com/example/tradeup/ui/messages/ChatViewModel.java
// << PHIÊN BẢN NÂNG CẤP HOÀN CHỈNH >>

package com.example.tradeup.ui.messages;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.ParticipantInfoDetail; // *** THÊM IMPORT NÀY ***
import com.example.tradeup.data.model.User;                   // *** THÊM IMPORT NÀY ***
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.UserRepository;     // *** THÊM IMPORT NÀY ***
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends ViewModel {
    private static final String TAG = "ChatViewModel";

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository; // *** THÊM REPOSITORY NÀY ***

    private final MutableLiveData<ChatState> _chatState = new MutableLiveData<>();
    public LiveData<ChatState> getChatState() {
        return _chatState;
    }

    private ListenerRegistration chatListenerRegistration;

    @Inject
    public ChatViewModel(ChatRepository chatRepository, AuthRepository authRepository, ItemRepository itemRepository, UserRepository userRepository) { // *** INJECT USER REPO ***
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository; // *** KHỞI TẠO ***
        listenForChats();
    }

    // === THAY THẾ HOÀN TOÀN HÀM NÀY ===
    private void listenForChats() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _chatState.setValue(new ChatState.Error("Please log in to see your messages."));
            return;
        }

        _chatState.setValue(new ChatState.Loading());

        chatListenerRegistration = chatRepository.getChatList(currentUser.getUid(), new Callback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> chats) {
                if (chats == null || chats.isEmpty()) {
                    _chatState.postValue(new ChatState.Empty());
                } else {
                    // Thay vì gọi loadRelatedItemsForChats, gọi hàm "làm giàu" mới
                    enrichChatsWithParticipantInfo(chats);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error listening for chats", e);
                _chatState.postValue(new ChatState.Error("Failed to load conversations."));
            }
        });
    }

    // === THÊM HÀM MỚI NÀY ===
    private void enrichChatsWithParticipantInfo(List<Chat> chats) {
        if (chats.isEmpty()) {
            loadRelatedItemsForChats(chats); // Gọi tiếp hàm cũ với danh sách rỗng
            return;
        }

        final List<Chat> enrichedChats = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger counter = new AtomicInteger(chats.size());
        final String currentUserId = authRepository.getCurrentUser().getUid();

        for (Chat chat : chats) {
            // Nếu chat đã có participantInfo đầy đủ, không cần làm gì thêm
            if (chat.getParticipantInfo() != null && chat.getParticipantInfo().size() == 2) {
                enrichedChats.add(chat);
                if (counter.decrementAndGet() == 0) {
                    loadRelatedItemsForChats(enrichedChats);
                }
                continue;
            }

            // Nếu thiếu, bắt đầu quá trình "làm giàu"
            String user1Id = chat.getParticipants().get(0);
            String user2Id = chat.getParticipants().get(1);

            // Dùng CompletableFuture để lấy thông tin cả 2 user song song
            userRepository.getUserProfile(user1Id).whenComplete((user1, throwable1) -> {
                userRepository.getUserProfile(user2Id).whenComplete((user2, throwable2) -> {
                    if (user1 != null && user2 != null) {
                        Map<String, ParticipantInfoDetail> participantInfoMap = new HashMap<>();
                        participantInfoMap.put(user1Id, new ParticipantInfoDetail(user1.getDisplayName(), user1.getProfilePictureUrl()));
                        participantInfoMap.put(user2Id, new ParticipantInfoDetail(user2.getDisplayName(), user2.getProfilePictureUrl()));

                        // Cập nhật lại đối tượng chat
                        chat.setParticipantInfo(participantInfoMap);
                    } else {
                        Log.w(TAG, "Could not enrich chat " + chat.getChatId() + ", one or more users not found.");
                    }

                    enrichedChats.add(chat); // Thêm chat (dù có làm giàu được hay không)
                    if (counter.decrementAndGet() == 0) {
                        loadRelatedItemsForChats(enrichedChats);
                    }
                });
            });
        }
    }


    // Hàm loadRelatedItemsForChats giữ nguyên, không cần thay đổi
    private void loadRelatedItemsForChats(List<Chat> chats) {
        // ... (Code của bạn trong hàm này đã đúng, giữ nguyên)
        if (chats.isEmpty()) {
            _chatState.postValue(new ChatState.Success(Collections.emptyList()));
            return;
        }

        final List<ChatViewData> chatViewDataList = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger counter = new AtomicInteger(chats.size());

        for (Chat chat : chats) {
            if (chat.getRelatedItemId() == null || chat.getRelatedItemId().isEmpty()) {
                chatViewDataList.add(new ChatViewData(chat, null));
                if (counter.decrementAndGet() == 0) {
                    _chatState.postValue(new ChatState.Success(chatViewDataList));
                }
            } else {
                itemRepository.getItemById(chat.getRelatedItemId(), new Callback<Item>() {
                    @Override
                    public void onSuccess(Item item) {
                        chatViewDataList.add(new ChatViewData(chat, item));
                        if (counter.decrementAndGet() == 0) {
                            _chatState.postValue(new ChatState.Success(chatViewDataList));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        chatViewDataList.add(new ChatViewData(chat, null));
                        if (counter.decrementAndGet() == 0) {
                            _chatState.postValue(new ChatState.Success(chatViewDataList));
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
    }
}