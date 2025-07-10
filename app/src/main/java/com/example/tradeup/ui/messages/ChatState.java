// File: src/main/java/com/example/tradeup/ui/messages/ChatState.java
package com.example.tradeup.ui.messages;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Chat;
import java.util.List;

public abstract class ChatState {
    private ChatState() {} // Private constructor

    // Trạng thái đang tải dữ liệu lần đầu
    public static final class Loading extends ChatState {}

    // Trạng thái tải thành công, chứa danh sách các cuộc trò chuyện
    public static final class Success extends ChatState {
        @NonNull
        public final List<Chat> chats;

        public Success(@NonNull List<Chat> chats) {
            this.chats = chats;
        }
    }

    // Trạng thái khi không có cuộc trò chuyện nào
    public static final class Empty extends ChatState {}

    // Trạng thái khi có lỗi xảy ra
    public static final class Error extends ChatState {
        @NonNull
        public final String message;

        public Error(@NonNull String message) {
            this.message = message;
        }
    }
}