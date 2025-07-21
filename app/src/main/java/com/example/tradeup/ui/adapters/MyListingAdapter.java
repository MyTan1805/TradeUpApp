package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.ItemMyListingBinding;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class MyListingAdapter extends ListAdapter<Item, MyListingAdapter.MyListingViewHolder> {

    private final OnItemActionListener listener;

    public interface OnItemActionListener {
        void onMenuClick(Item item);
        void onRateBuyerClick(Item item);
        void onItemClick(Item item);
    }

    // ==========================================================
    // === BƯỚC SỬA LỖI: DI CHUYỂN KHỐI NÀY LÊN TRÊN ============
    // ==========================================================
    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getItemId().equals(newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            // So sánh các trường có thể thay đổi và ảnh hưởng đến UI
            return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                    Objects.equals(oldItem.getViewsCount(), newItem.getViewsCount()) &&
                    Objects.equals(oldItem.getOffersCount(), newItem.getOffersCount()) &&
                    Objects.equals(oldItem.getChatsCount(), newItem.getChatsCount());
        }
    };

    public MyListingAdapter(@NonNull OnItemActionListener listener) {
        // Bây giờ, khi gọi super(), DIFF_CALLBACK đã được định nghĩa ở trên
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingBinding binding = ItemMyListingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        Item item = getItem(position);
        if (item != null) {
            holder.bind(item, listener);
        }
    }

    static class MyListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;

        MyListingViewHolder(ItemMyListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Item item, final OnItemActionListener listener) {
            // --- 1. BIND DỮ LIỆU CƠ BẢN ---
            binding.textViewProductName.setText(item.getTitle());
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewProductPrice.setText(currencyFormat.format(item.getPrice()));
            binding.chipProductStatus.setText(item.getStatus());

            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_placeholder_image)
                        .centerCrop()
                        .into(binding.imageViewProduct);
            } else {
                binding.imageViewProduct.setImageResource(R.drawable.ic_placeholder_image);
            }

            // --- 2. BIND SỐ LIỆU THỐNG KÊ (ANALYTICS) ---
            long viewsCount = item.getViewsCount();
            binding.textViewViews.setText(String.valueOf(viewsCount));

            long offersCount = item.getOffersCount();
            binding.textViewOffers.setText(String.valueOf(offersCount));

            long chatsCount = item.getChatsCount();
            binding.textViewChats.setText(String.valueOf(chatsCount));

            // --- 3. LOGIC HIỂN THỊ CÁC THÀNH PHẦN GIAO DIỆN ---
            boolean isSold = "sold".equalsIgnoreCase(item.getStatus());

            binding.buttonMenu.setVisibility(View.VISIBLE);
            binding.buttonRateBuyer.setVisibility(isSold ? View.VISIBLE : View.GONE);
            binding.textViewViews.setVisibility(viewsCount > 0 ? View.VISIBLE : View.GONE);
            binding.textViewOffers.setVisibility(offersCount > 0 ? View.VISIBLE : View.GONE);
            binding.textViewChats.setVisibility(chatsCount > 0 ? View.VISIBLE : View.GONE);

            // --- 4. GÁN SỰ KIỆN CLICK ---
            itemView.setOnClickListener(v -> listener.onItemClick(item));
            binding.buttonMenu.setOnClickListener(v -> listener.onMenuClick(item));
            binding.buttonRateBuyer.setOnClickListener(v -> listener.onRateBuyerClick(item));
        }
    }
}