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
import com.example.tradeup.databinding.ItemMessageImageReceivedBinding;
import com.example.tradeup.databinding.ItemMessageImageSentBinding;
import com.example.tradeup.databinding.ItemMessageReceivedBinding;
import com.example.tradeup.databinding.ItemMessageSentBinding;

import androidx.emoji2.text.EmojiCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ChatDetailAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    // === KHAI BÁO LẠI CÁC VIEWTYPE CHO CHÍNH XÁC ===
    private static final int VIEW_TYPE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    private static final int VIEW_TYPE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    private final String currentUserId;
    private final String otherUserAvatarUrl;

    public ChatDetailAdapter(String currentUserId, String otherUserAvatarUrl) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.otherUserAvatarUrl = otherUserAvatarUrl;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        boolean isSentByMe = message.getSenderId().equals(currentUserId);
        boolean isImageType = "image".equals(message.getType());

        if (isSentByMe) {
            return isImageType ? VIEW_TYPE_SENT_IMAGE : VIEW_TYPE_SENT_TEXT;
        } else {
            return isImageType ? VIEW_TYPE_RECEIVED_IMAGE : VIEW_TYPE_RECEIVED_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_SENT_TEXT:
                return new SentMessageViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_RECEIVED_TEXT:
                return new ReceivedMessageViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_SENT_IMAGE:
                return new SentImageViewHolder(ItemMessageImageSentBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_RECEIVED_IMAGE:
                return new ReceivedImageViewHolder(ItemMessageImageReceivedBinding.inflate(inflater, parent, false));
            default:
                // Fallback, không nên xảy ra
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT_TEXT:
                ((SentMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_RECEIVED_TEXT:
                ((ReceivedMessageViewHolder) holder).bind(message, otherUserAvatarUrl);
                break;
            case VIEW_TYPE_SENT_IMAGE:
                ((SentImageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_RECEIVED_IMAGE:
                ((ReceivedImageViewHolder) holder).bind(message, otherUserAvatarUrl);
                break;
        }
    }

    // --- CÁC LỚP VIEWHOLDER (GIỮ NGUYÊN NHƯ BẠN ĐÃ CÓ) ---

    // ViewHolder cho tin nhắn văn bản gửi đi
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;
        SentMessageViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(Message message) {
            // << SỬA Ở ĐÂY >>
            // Chỉ hiển thị text nếu nó không null
            if (message.getText() != null) {
                // Xử lý chuỗi để chuyển các ký tự emoji thành hình ảnh có thể vẽ được
                binding.textViewMessage.setText(EmojiCompat.get().process(message.getText()));
            } else {
                binding.textViewMessage.setText("");
            }

            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            }
        }
    }

    // ViewHolder cho tin nhắn văn bản nhận được
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;
        ReceivedMessageViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(Message message, String avatarUrl) {
            // << SỬA Ở ĐÂY >>
            if (message.getText() != null) {
                binding.textViewMessage.setText(EmojiCompat.get().process(message.getText()));
            } else {
                binding.textViewMessage.setText("");
            }

            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            }
            Glide.with(itemView.getContext()).load(avatarUrl).placeholder(R.drawable.ic_person).into(binding.imageViewAvatar);
        }
    }

    // ViewHolder cho tin nhắn ảnh gửi đi
    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageImageSentBinding binding;
        SentImageViewHolder(ItemMessageImageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(Message message) {
            Glide.with(itemView.getContext()).load(message.getImageUrl()).placeholder(R.color.grey_200).into(binding.imageViewSent);
            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            }
        }
    }

    // ViewHolder cho tin nhắn ảnh nhận được
    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageImageReceivedBinding binding;
        ReceivedImageViewHolder(ItemMessageImageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(Message message, String avatarUrl) {
            Glide.with(itemView.getContext()).load(message.getImageUrl()).placeholder(R.color.grey_200).into(binding.imageViewReceived);
            Glide.with(itemView.getContext()).load(avatarUrl).placeholder(R.drawable.ic_person).into(binding.imageViewAvatar);
            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            }
        }
    }

    // --- DiffUtil ---
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            // MessageId nên được tự động tạo bởi Firestore khi thêm document, không nên null
            if (oldItem.getMessageId() != null && newItem.getMessageId() != null) {
                return oldItem.getMessageId().equals(newItem.getMessageId());
            }
            // Fallback nếu vì lý do nào đó ID là null
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getText(), newItem.getText()) &&
                    Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl()) &&
                    Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp());
        }
    };
}