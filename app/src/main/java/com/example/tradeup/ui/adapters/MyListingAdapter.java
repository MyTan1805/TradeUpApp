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

public class MyListingAdapter extends ListAdapter<Item, MyListingAdapter.MyListingViewHolder> {

    private final OnItemActionListener listener;

    // Interface để fragment lắng nghe các hành động của người dùng trên mỗi item
    public interface OnItemActionListener {
        void onMenuClick(Item item);
        void onRateBuyerClick(Item item);
        void onItemClick(Item item);
    }

    public MyListingAdapter(@NonNull OnItemActionListener listener) {
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
        holder.bind(getItem(position), listener);
    }

    static class MyListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;

        MyListingViewHolder(ItemMyListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Item item, final OnItemActionListener listener) {
            // Bind thông tin cơ bản
            binding.textViewProductName.setText(item.getTitle());
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewProductPrice.setText(currencyFormat.format(item.getPrice()));

            // Bind ảnh đầu tiên
            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_image_not_found)
                        .centerCrop()
                        .into(binding.imageViewProduct);
            } else {
                binding.imageViewProduct.setImageResource(R.drawable.ic_placeholder_image);
            }

            // Bind trạng thái và số lượt xem/trả giá
            binding.chipProductStatus.setText(item.getStatus());

            // << FIX: KIỂM TRA NULL CHO VIEWSCOUNT >>
            if (item.getViewsCount() != null) {
                String viewsText = String.format(Locale.getDefault(), "%d views", item.getViewsCount());
                binding.textViewViews.setText(viewsText);
                binding.textViewViews.setVisibility(View.VISIBLE);
            } else {
                binding.textViewViews.setVisibility(View.GONE);
            }

            // Gán sự kiện click cho các nút và toàn bộ item
            itemView.setOnClickListener(v -> listener.onItemClick(item));
            binding.buttonMenu.setOnClickListener(v -> listener.onMenuClick(item));
            binding.buttonRateBuyer.setOnClickListener(v -> listener.onRateBuyerClick(item));

            // Hiển thị nút "Rate Buyer" chỉ khi item đã được bán và chưa được đánh giá
            boolean isSold = "sold".equalsIgnoreCase(item.getStatus());
            if ("sold".equalsIgnoreCase(item.getStatus())) {
                binding.buttonRateBuyer.setVisibility(View.VISIBLE);
                binding.buttonRateBuyer.setOnClickListener(v -> listener.onRateBuyerClick(item));
            } else {
                binding.buttonRateBuyer.setVisibility(View.GONE);
            }
            boolean needsRating = true;
            binding.buttonRateBuyer.setVisibility(isSold && needsRating ? View.VISIBLE : View.GONE);
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getItemId().equals(newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            // Sử dụng equals() đã được override trong Item.java
            return oldItem.equals(newItem);
        }
    };
}