// File: src/main/java/com/example/tradeup/ui/adapters/NotificationAdapter.java
package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Notification;
import com.example.tradeup.databinding.ItemNotificationBinding;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.NotificationViewHolder> {

    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        // ViewHolder không cần biết về listener nữa
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        // Lấy đối tượng notification hiện tại
        Notification currentNotification = getItem(position);
        // Gọi hàm bind để cập nhật UI
        holder.bind(currentNotification);
        // << FIX LỖI STATIC CONTEXT: Thiết lập listener ở đây >>
        // Tại đây, chúng ta có cả `holder` và `currentNotification`
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(currentNotification);
            }
        });
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        // Constructor giờ chỉ cần binding
        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Hàm bind giờ chỉ nhận notification và chịu trách nhiệm cập nhật UI
        void bind(final Notification notification) {
            // << FIX LỖI 1: Dùng getMessage() >>
            binding.textViewContent.setText(notification.getMessage());
            binding.textViewTimestamp.setText(formatTimestamp(notification.getCreatedAt()));

            int iconRes = getIconForType(notification.getType());
            binding.imageViewIcon.setImageResource(iconRes);
            binding.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        }

        private String formatTimestamp(Timestamp timestamp) {
            if (timestamp == null) return "";
            long diffInMillis = new Date().getTime() - timestamp.toDate().getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            if (diffInMinutes < 1) return "Just now";
            if (diffInMinutes < 60) return diffInMinutes + "m ago";
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            if (diffInHours < 24) return diffInHours + "h ago";
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            if (diffInDays < 7) return diffInDays + "d ago";
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(timestamp.toDate());
        }

        private int getIconForType(String type) {
            if (type == null) return R.drawable.ic_notifications;
            switch (type) {
                case "new_offer":
                case "offer_accepted":
                    return R.drawable.ic_local_offer;
                case "new_message":
                    return R.drawable.ic_chat;
                case "listing_update":
                    return R.drawable.ic_inventory;
                case "promotion":
                    return R.drawable.ic_campaign;
                default:
                    return R.drawable.ic_notifications;
            }
        }
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Notification>() {
                @Override
                public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    // << FIX LỖI 1: Dùng getId() >>
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    // << FIX LỖI 1: Dùng getMessage() >>
                    return oldItem.isRead() == newItem.isRead() &&
                            Objects.equals(oldItem.getMessage(), newItem.getMessage());
                }
            };
}