package com.example.tradeup.ui.adapters;// package: com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.ItemMyListingBinding;

public class MyListingAdapter extends ListAdapter<Item, MyListingAdapter.MyListingViewHolder> {

    private final OnItemMenuClickListener listener;

    public interface OnItemMenuClickListener {
        void onMenuClick(Item item);
    }

    public MyListingAdapter(OnItemMenuClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingBinding binding = ItemMyListingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyListingViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class MyListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;
        private final OnItemMenuClickListener listener;

        MyListingViewHolder(ItemMyListingBinding binding, OnItemMenuClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Item item) {
            binding.textViewProductName.setText(item.getTitle());
            binding.textViewProductPrice.setText(String.format("$%.2f", item.getPrice())); // Định dạng giá

            // Lấy ảnh đầu tiên trong danh sách
            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_image_not_found)
                        .into(binding.imageViewProduct);
            }

            // TODO: Cập nhật chip trạng thái và số lượt xem
            // binding.chipProductStatus.setText(item.getStatus());
            // binding.textViewViews.setText(String.format("%d views", item.getViews()));

            binding.buttonMenu.setOnClickListener(v -> listener.onMenuClick(item));
        }
    }

    // DiffUtil giúp RecyclerView cập nhật danh sách hiệu quả
    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.equals(newItem);
        }
    };
}