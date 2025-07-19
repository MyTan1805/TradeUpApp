// File: src/main/java/com/example/tradeup/ui/messages/ChatViewModel.java
package com.example.tradeup.ui.messages;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends ViewModel {
    private static final String TAG = "ChatViewModel";

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final ItemRepository itemRepository;

    private final MutableLiveData<ChatState> _chatState = new MutableLiveData<>();
    public LiveData<ChatState> getChatState() {
        return _chatState;
    }

    private ListenerRegistration chatListenerRegistration;

    @Inject
    public ChatViewModel(ChatRepository chatRepository, AuthRepository authRepository, ItemRepository itemRepository) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        this.itemRepository = itemRepository;
        listenForChats();
    }

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
                    loadRelatedItemsForChats(chats);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error listening for chats", e);
                _chatState.postValue(new ChatState.Error("Failed to load conversations."));
            }
        });
    }

    private void loadRelatedItemsForChats(List<Chat> chats) {
        List<ChatViewData> chatViewDataList = new ArrayList<>();
        if (chats.isEmpty()) {
            _chatState.postValue(new ChatState.Success(Collections.emptyList()));
            return;
        }

        AtomicInteger counter = new AtomicInteger(chats.size());

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