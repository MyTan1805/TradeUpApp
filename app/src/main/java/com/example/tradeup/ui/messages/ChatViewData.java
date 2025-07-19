package com.example.tradeup.ui.messages;

import androidx.annotation.Nullable;

import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.Item;

public class ChatViewData {
    public final Chat chat;
    @Nullable // Item có thể null nếu không tìm thấy
    public final Item relatedItem;

    public ChatViewData(Chat chat, @Nullable Item relatedItem) {
        this.chat = chat;
        this.relatedItem = relatedItem;
    }
}