// File: src/main/java/com/example/tradeup/ui/adapters/ChatDetailAdapter.java
package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Message;
import com.example.tradeup.databinding.ItemMessageReceivedBinding;
import com.example.tradeup.databinding.ItemMessageSentBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ChatDetailAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final String currentUserId;
    private final String otherUserAvatarUrl; // URL ảnh của người đối diện

    public ChatDetailAdapter(String currentUserId, String otherUserAvatarUrl) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.otherUserAvatarUrl = otherUserAvatarUrl;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(inflater, parent, false);
            return new SentMessageViewHolder(binding);
        } else { // VIEW_TYPE_RECEIVED
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(inflater, parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, otherUserAvatarUrl);
        }
    }

    // ViewHolder cho tin nhắn gửi đi
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        SentMessageViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.textViewMessage.setText(message.getText());
            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            }
        }
    }

    // ViewHolder cho tin nhắn nhận được
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        ReceivedMessageViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message, String avatarUrl) {
            binding.textViewMessage.setText(message.getText());
            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            }

            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(binding.imageViewAvatar);
        }
    }

    // DiffUtil để RecyclerView cập nhật hiệu quả
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getMessageId().equals(newItem.getMessageId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getText(), newItem.getText()) &&
                    Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp());
        }
    };
}