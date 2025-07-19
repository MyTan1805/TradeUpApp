// File: src/main/java/com/example/tradeup/ui/adapters/ChatAdapter.java
package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.ParticipantInfoDetail;
import com.example.tradeup.databinding.ItemChatConversationBinding;
import com.example.tradeup.ui.messages.ChatViewData;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.Objects;

// *** ĐẢM BẢO Generic Type là ChatViewData ***
public class ChatAdapter extends ListAdapter<ChatViewData, ChatAdapter.ChatViewHolder> {

    private final OnChatClickListener listener;
    private final String currentUserId;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(Context context, OnChatClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatConversationBinding binding = ItemChatConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        // *** SỬA LỖI Ở ĐÂY: Truyền đúng 3 tham số ***
        return new ChatViewHolder(binding, listener, currentUserId);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatConversationBinding binding;
        private final OnChatClickListener listener;
        private final String currentUserId;

        // *** SỬA LỖI Ở ĐÂY: Constructor nhận đúng 3 tham số ***
        ChatViewHolder(ItemChatConversationBinding binding, OnChatClickListener listener, String currentUserId) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.currentUserId = currentUserId;
        }

        void bind(final ChatViewData data) {
            final Chat chat = data.chat;

            itemView.setOnClickListener(v -> listener.onChatClick(chat));

            String otherUserId = chat.getParticipants().stream()
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (otherUserId != null && chat.getParticipantInfo() != null) {
                ParticipantInfoDetail otherUserInfo = chat.getParticipantInfo().get(otherUserId);
                if (otherUserInfo != null) {
                    binding.textViewUserName.setText(otherUserInfo.getDisplayName());
                    Glide.with(itemView.getContext())
                            .load(otherUserInfo.getProfilePictureUrl())
                            .placeholder(R.drawable.ic_person)
                            .into(binding.imageViewAvatar);
                }
            }

            binding.textViewLastMessage.setText(chat.getLastMessageText());

            if (chat.getLastMessageTimestamp() != null) {
                binding.textViewTimestamp.setText(
                        DateUtils.getRelativeTimeSpanString(
                                chat.getLastMessageTimestamp().toDate().getTime(),
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS)
                );
            }

            Map<String, Integer> unreadCountMap = chat.getUnreadCount();
            Integer unreadCount = (unreadCountMap != null) ? unreadCountMap.get(currentUserId) : null;

            if (unreadCount != null && unreadCount > 0) {
                binding.unreadCountBadge.setVisibility(View.VISIBLE);
                binding.unreadCountBadge.setText(String.valueOf(unreadCount));
                binding.textViewLastMessage.setTypeface(null, Typeface.BOLD);
            } else {
                binding.unreadCountBadge.setVisibility(View.GONE);
                binding.textViewLastMessage.setTypeface(null, Typeface.NORMAL);
            }

            if (data.relatedItem != null) {
                binding.groupItemContext.setVisibility(View.VISIBLE);
                binding.textViewItemName.setText(data.relatedItem.getTitle());
                if (currentUserId.equals(data.relatedItem.getSellerId())) {
                    binding.textViewContext.setText("selling: ");
                } else {
                    binding.textViewContext.setText("buying: ");
                }
            } else {
                binding.groupItemContext.setVisibility(View.GONE);
            }
        }
    }

    private static final DiffUtil.ItemCallback<ChatViewData> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatViewData>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatViewData oldItem, @NonNull ChatViewData newItem) {
            return oldItem.chat.getChatId().equals(newItem.chat.getChatId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatViewData oldItem, @NonNull ChatViewData newItem) {
            return Objects.equals(oldItem.chat.getLastMessageText(), newItem.chat.getLastMessageText()) &&
                    Objects.equals(oldItem.chat.getLastMessageTimestamp(), newItem.chat.getLastMessageTimestamp()) &&
                    Objects.equals(oldItem.chat.getUnreadCount(), newItem.chat.getUnreadCount());
        }
    };
}