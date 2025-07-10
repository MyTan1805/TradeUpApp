package com.example.tradeup.data.source.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Chat;    // Model Chat (Java)
import com.example.tradeup.data.model.Message;  // Model Message (Java)
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class FirebaseChatSource {

    private final FirebaseFirestore firestore;
    private final CollectionReference chatsCollection;

    @Inject
    public FirebaseChatSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.chatsCollection = firestore.collection("chats");
    }

    public void getOrCreateChat(List<String> participantIds, @Nullable String relatedItemId, Callback<String> callback) {
        if (participantIds == null || participantIds.size() != 2) {
            callback.onFailure(new IllegalArgumentException("Chat must have exactly 2 participants."));
            return;
        }

        List<String> sortedIds = new ArrayList<>(participantIds);
        Collections.sort(sortedIds);
        // Cân nhắc việc có nên bao gồm relatedItemId trong chatId hay không.
        // Nếu mỗi sản phẩm có 1 chat riêng giữa 2 người:
        // String chatId = sortedIds.get(0) + "_" + sortedIds.get(1) + (relatedItemId != null ? "_" + relatedItemId : "");
        // Nếu chỉ có 1 chat duy nhất giữa 2 người:
        String chatId = sortedIds.get(0) + "_" + sortedIds.get(1);


        DocumentReference chatDocRef = chatsCollection.document(chatId);
        chatDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    callback.onSuccess(chatId); // Chat đã tồn tại
                } else {
                    // Tạo chat mới
                    Chat newChat = new Chat();

                    // KHÔNG CẦN DÒNG NÀY NỮA, VÌ @DocumentId SẼ TỰ LÀM:
                    // newChat.setChatId(chatId);

                    newChat.setParticipants(sortedIds);
                    if (relatedItemId != null) {
                        newChat.setRelatedItemId(relatedItemId);
                    }
                    // Các trường khác như participantInfo, unreadCount...

                    // Khi set(), Firestore sẽ tự động điền ID document vào trường có @DocumentId
                    chatDocRef.set(newChat)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(chatId))
                            .addOnFailureListener(callback::onFailure);
                }
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public ListenerRegistration getChatList(String userId, Callback<List<Chat>> callback) {
        return chatsCollection
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }
                    if (snapshots != null) {
                        List<Chat> chats = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Chat chat = doc.toObject(Chat.class);
                            if (chat != null) {
                                // Firestore không tự gán ID document vào object khi dùng toObject với listener
                                // Nếu Chat.java của bạn có setter cho chatId và field chatId
                                // chat.setChatId(doc.getId()); // Gán ID document thủ công
                                chats.add(chat);
                            }
                        }
                        callback.onSuccess(chats);
                    }
                });
    }

    public ListenerRegistration getMessages(String chatId, Callback<List<Message>> callback) {
        return chatsCollection.document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }
                    if (snapshots != null) {
                        List<Message> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                // message.setMessageId(doc.getId()); // Gán ID nếu cần
                                messages.add(message);
                            }
                        }
                        callback.onSuccess(messages);
                    }
                });
    }

    public void sendMessage(String chatId, Message message, Callback<Void> callback) {
        CollectionReference messagesSubCollection = chatsCollection.document(chatId).collection("messages");

        WriteBatch batch = firestore.batch();

        // 1. Thêm tin nhắn mới vào subcollection
        // Dùng trực tiếp object Message vì nó đã có @ServerTimestamp cho trường timestamp
        DocumentReference newMessageRef = messagesSubCollection.document();
        batch.set(newMessageRef, message);

        // 2. Cập nhật thông tin trong document chat chính
        DocumentReference chatDocRef = chatsCollection.document(chatId);
        Map<String, Object> chatUpdates = new HashMap<>();

        String lastMessageText = message.getText();
        if (lastMessageText == null && message.getImageUrl() != null) {
            lastMessageText = "Image";
        } else if (lastMessageText == null && message.getOfferDetails() != null) {
            lastMessageText = "Offer";
        }

        chatUpdates.put("lastMessageText", lastMessageText);
        chatUpdates.put("lastMessageTimestamp", FieldValue.serverTimestamp());
        chatUpdates.put("lastMessageSenderId", message.getSenderId());
        chatUpdates.put("updatedAt", FieldValue.serverTimestamp());

        // === PHẦN SỬA LỖI & THÊM MỚI QUAN TRỌNG NHẤT ===
        // Tăng unreadCount của người nhận (receiver) lên 1
        if (message.getReceiverId() != null) {
            // Dùng dot notation để cập nhật một trường bên trong một map
            // Ví dụ: "unreadCount.user_id_B"
            chatUpdates.put("unreadCount." + message.getReceiverId(), FieldValue.increment(1));
        }
        // === KẾT THÚC PHẦN SỬA LỖI ===

        batch.update(chatDocRef, chatUpdates);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseChatSource", "sendMessage batch commit successful.");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseChatSource", "sendMessage batch commit failed.", e);
                    callback.onFailure(e);
                });
    }

    public void markMessagesAsRead(String chatId, String userId, Callback<Void> callback) {
        // Logic này có thể phức tạp:
        // 1. Cập nhật trường unreadCount.<userId> = 0 trong Chat document.
        // 2. (Tùy chọn) Query các tin nhắn chưa đọc của user đó trong subcollection messages và update isRead = true.
        // Bước 1 thường đủ cho việc hiển thị số lượng tin nhắn chưa đọc.

        chatsCollection.document(chatId)
                .update("unreadCount." + userId, 0) // Ví dụ: "unreadCount.userId123"
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}