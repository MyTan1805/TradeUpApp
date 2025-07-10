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
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.ParticipantInfoDetail;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.databinding.ItemChatConversationBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

public class ChatAdapter extends ListAdapter<Chat, ChatAdapter.ChatViewHolder> {

    private final OnChatClickListener listener;
    private final String currentUserId;
    private final ItemRepository itemRepository;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    // EntryPoint để lấy ItemRepository từ Hilt
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface AdapterEntryPoint {
        ItemRepository itemRepository();
    }

    public ChatAdapter(Context context, OnChatClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Lấy ItemRepository từ Hilt
        AdapterEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.getApplicationContext(),
                AdapterEntryPoint.class
        );
        this.itemRepository = hiltEntryPoint.itemRepository();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatConversationBinding binding = ItemChatConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ChatViewHolder(binding, listener, currentUserId, itemRepository);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatConversationBinding binding;
        private final OnChatClickListener listener;
        private final String currentUserId;
        private final ItemRepository itemRepository;

        ChatViewHolder(ItemChatConversationBinding binding, OnChatClickListener listener, String currentUserId, ItemRepository itemRepository) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.currentUserId = currentUserId;
            this.itemRepository = itemRepository;
        }

        void bind(final Chat chat) {
            itemView.setOnClickListener(v -> listener.onChatClick(chat));

            // Tìm thông tin của người đối diện
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

            // Hiển thị tin nhắn cuối
            binding.textViewLastMessage.setText(chat.getLastMessageText());

            // Hiển thị thời gian
            if (chat.getLastMessageTimestamp() != null) {
                binding.textViewTimestamp.setText(
                        DateUtils.getRelativeTimeSpanString(
                                chat.getLastMessageTimestamp().toDate().getTime(),
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS)
                );
            }

            // === PHẦN XỬ LÝ UNREAD COUNT (ĐÃ ĐƯỢC SỬA GỌN) ===
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
            // === KẾT THÚC PHẦN SỬA ===

            // Hiển thị context về sản phẩm (nếu có)
            if (chat.getRelatedItemId() != null && !chat.getRelatedItemId().isEmpty()) {
                binding.groupItemContext.setVisibility(View.VISIBLE);
                itemRepository.getItemById(chat.getRelatedItemId(), new com.example.tradeup.core.utils.Callback<Item>() {
                    @Override
                    public void onSuccess(Item item) {
                        if (binding == null) return;
                        if (item != null) {
                            binding.textViewItemName.setText(item.getTitle());
                            if (currentUserId.equals(item.getSellerId())) {
                                binding.textViewContext.setText("selling: ");
                            } else {
                                binding.textViewContext.setText("buying: ");
                            }
                        } else {
                            binding.groupItemContext.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (binding != null) {
                            binding.groupItemContext.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                binding.groupItemContext.setVisibility(View.GONE);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Chat> DIFF_CALLBACK = new DiffUtil.ItemCallback<Chat>() {
        @Override
        public boolean areItemsTheSame(@NonNull Chat oldItem, @NonNull Chat newItem) {
            return oldItem.getChatId().equals(newItem.getChatId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Chat oldItem, @NonNull Chat newItem) {
            return Objects.equals(oldItem.getLastMessageText(), newItem.getLastMessageText()) &&
                    Objects.equals(oldItem.getLastMessageTimestamp(), newItem.getLastMessageTimestamp()) &&
                    Objects.equals(oldItem.getUnreadCount(), newItem.getUnreadCount());
        }
    };
}