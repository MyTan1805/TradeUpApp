package com.example.tradeup.data.source.remote;

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
                    // Bạn cần tạo class Chat.java với constructor phù hợp
                    Chat newChat = new Chat(); // Giả sử Chat.java có constructor rỗng
                    newChat.setChatId(chatId);
                    newChat.setParticipants(sortedIds);
                    if (relatedItemId != null) {
                        newChat.setRelatedItemId(relatedItemId);
                    }
                    // Khởi tạo các trường khác của Chat nếu cần (ví dụ: createdAt, updatedAt dùng FieldValue.serverTimestamp())
                    // newChat.setCreatedAt(FieldValue.serverTimestamp()); // Sẽ lỗi nếu model không hỗ trợ Object
                    // newChat.setUpdatedAt(FieldValue.serverTimestamp());

                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("chatId", chatId); // Firestore sẽ tự lấy từ ID document nếu model có @DocumentId
                    chatData.put("participants", sortedIds);
                    if (relatedItemId != null) {
                        chatData.put("relatedItemId", relatedItemId);
                    }
                    chatData.put("createdAt", FieldValue.serverTimestamp());
                    chatData.put("updatedAt", FieldValue.serverTimestamp());
                    // Khởi tạo participantInfo và unreadCount nếu cần
                    // Map<String, Object> participantInfo = new HashMap<>(); // Cần lấy info từ users collection
                    // chatData.put("participantInfo", participantInfo);
                    // Map<String, Integer> unreadCount = new HashMap<>();
                    // unreadCount.put(sortedIds.get(0), 0);
                    // unreadCount.put(sortedIds.get(1), 0);
                    // chatData.put("unreadCount", unreadCount);


                    chatDocRef.set(chatData) // Dùng Map hoặc POJO Chat
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
        // Firestore sẽ tự tạo timestamp nếu model Message.java có field timestamp với @ServerTimestamp
        // Nếu không, bạn cần set thủ công: message.setTimestamp(Timestamp.now());
        // Hoặc dùng Map để set FieldValue.serverTimestamp() cho trường timestamp

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", message.getSenderId());
        messageData.put("receiverId", message.getReceiverId());
        if (message.getText() != null) messageData.put("text", message.getText());
        if (message.getImageUrl() != null) messageData.put("imageUrl", message.getImageUrl());
        messageData.put("type", message.getType());
        messageData.put("timestamp", FieldValue.serverTimestamp()); // Quan trọng để có server timestamp
        messageData.put("isRead", false);
        // Thêm offerDetails nếu có

        CollectionReference messagesSubCollection = chatsCollection.document(chatId).collection("messages");

        // Sử dụng batch write để thêm tin nhắn và cập nhật chat document
        WriteBatch batch = firestore.batch();

        // 1. Thêm tin nhắn mới
        DocumentReference newMessageRef = messagesSubCollection.document(); // Tự tạo ID cho tin nhắn
        batch.set(newMessageRef, messageData); // Dùng Map hoặc POJO Message

        // 2. Cập nhật thông tin lastMessage trong Chat document
        DocumentReference chatDocRef = chatsCollection.document(chatId);
        Map<String, Object> chatUpdates = new HashMap<>();
        String lastMessageText = message.getText();
        if (lastMessageText == null && message.getImageUrl() != null) {
            lastMessageText = "Hình ảnh"; // Hoặc "Image"
        } else if (lastMessageText == null && message.getOfferDetails() != null) {
            lastMessageText = "Đề nghị"; // Hoặc "Offer"
        }
        chatUpdates.put("lastMessageText", lastMessageText);
        chatUpdates.put("lastMessageTimestamp", FieldValue.serverTimestamp()); // Dùng server timestamp
        chatUpdates.put("lastMessageSenderId", message.getSenderId());
        chatUpdates.put("updatedAt", FieldValue.serverTimestamp());
        // Cập nhật unreadCount cho người nhận (ví dụ, tăng unreadCount của receiverId)
        // chatUpdates.put("unreadCount." + message.getReceiverId(), FieldValue.increment(1));

        batch.update(chatDocRef, chatUpdates);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
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