// File: src/main/java/com/example/tradeup/ui/adapters/NotificationAdapter.java
// << PHIÊN BẢN ĐÃ ĐỒNG BỘ VỚI LAYOUT MỚI CỦA BẠN >>

package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Notification;
import com.example.tradeup.databinding.ItemNotificationBinding; // Tên binding của bạn có thể khác

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.NotificationViewHolder> {

    private final OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        super(DIFF_CALLBACK);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // << SỬA Ở ĐÂY: Đảm bảo tên Binding khớp với tên file layout của bạn >>
        // Ví dụ: nếu file là item_notification.xml -> ItemNotificationBinding
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new NotificationViewHolder(binding, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;
        private final OnNotificationClickListener clickListener;

        public NotificationViewHolder(ItemNotificationBinding binding, OnNotificationClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = listener;
        }

        void bind(Notification notification) {
            if (notification == null) return;
            Context context = itemView.getContext();

            // === LOGIC BIND DỮ LIỆU MỚI ===

            // 1. Hiển thị nội dung chính
            // Chúng ta sẽ kết hợp title và message để hiển thị đẹp hơn
            SpannableStringBuilder contentBuilder = new SpannableStringBuilder();
            if (notification.getTitle() != null) {
                contentBuilder.append(notification.getTitle());
                // In đậm phần title
                contentBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, notification.getTitle().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                contentBuilder.append(" - ");
            }
            if (notification.getMessage() != null) {
                contentBuilder.append(notification.getMessage());
            }
            binding.textViewContent.setText(contentBuilder);

            // 2. Hiển thị thời gian
            if (notification.getCreatedAt() != null) {
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        notification.getCreatedAt().toDate().getTime(), now, DateUtils.MINUTE_IN_MILLIS);
                binding.textViewTimestamp.setText(relativeTime);
            } else {
                binding.textViewTimestamp.setText("");
            }

            // 3. Hiển thị chấm báo chưa đọc
            binding.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // 4. Hiển thị icon tương ứng với loại thông báo
            // Thay vì dùng ảnh sản phẩm, chúng ta sẽ dùng icon hệ thống cho gọn
            int iconResId = getIconForNotificationType(notification.getType());
            binding.imageViewIcon.setImageResource(iconResId);

            // 5. Bắt sự kiện click
            itemView.setOnClickListener(v -> {
                if(clickListener != null) {
                    clickListener.onNotificationClick(notification);
                }
            });
        }

        // Hàm tiện ích để chọn icon
        private int getIconForNotificationType(String type) {
            if (type == null) return R.drawable.ic_notifications;

            switch (type) {
                case "new_offer":
                case "offer_countered":
                    return R.drawable.ic_local_offer; // Icon thẻ giá
                case "offer_accepted":
                    return R.drawable.ic_check_circle; // Icon dấu tick
                case "offer_rejected":
                    return R.drawable.ic_cancel; // Icon dấu chéo
                case "new_message":
                    return R.drawable.ic_chat; // Icon tin nhắn
                default:
                    return R.drawable.ic_notifications; // Icon chuông mặc định
            }
        }
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Notification>() {
                @Override
                public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
                    return oldItem.isRead() == newItem.isRead() &&
                            java.util.Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                            java.util.Objects.equals(oldItem.getMessage(), newItem.getMessage());
                }
            };
}