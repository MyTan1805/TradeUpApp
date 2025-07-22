package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.ItemProductCardBinding;
import com.example.tradeup.databinding.ItemProductCardHorizontalBinding;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ProductAdapter extends ListAdapter<Item, RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_GRID = 1;
    public static final int VIEW_TYPE_HORIZONTAL = 2;

    private final int viewType;
    private final OnProductClickListener listener;
    private Set<String> savedItemIds = new HashSet<>();

    public interface OnProductClickListener {
        void onItemClick(Item item);
        void onFavoriteClick(Item item);
    }

    public ProductAdapter(int viewType, OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.viewType = viewType;
        this.listener = listener;
    }

    public void setSavedItemIds(Set<String> ids) {
        this.savedItemIds = (ids != null) ? ids : new HashSet<>();
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HORIZONTAL) {
            ItemProductCardHorizontalBinding binding = ItemProductCardHorizontalBinding.inflate(inflater, parent, false);
            return new HorizontalProductViewHolder(binding);
        } else {
            ItemProductCardBinding binding = ItemProductCardBinding.inflate(inflater, parent, false);
            return new GridProductViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item currentItem = getItem(position);
        if (currentItem == null) return; // An toàn hơn

        boolean isSaved = savedItemIds.contains(currentItem.getItemId());

        if (holder instanceof GridProductViewHolder) {
            ((GridProductViewHolder) holder).bind(currentItem, isSaved, listener);
        } else if (holder instanceof HorizontalProductViewHolder) {
            ((HorizontalProductViewHolder) holder).bind(currentItem, listener);
        }
    }

    static class GridProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;

        GridProductViewHolder(ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // << PHIÊN BẢN ĐÃ ĐƯỢC DỌN DẸP >>
        void bind(final Item item, boolean isSaved, final OnProductClickListener listener) {
            // Bind data (giữ nguyên)
            binding.textViewProductName.setText(item.getTitle());
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            binding.textViewProductPrice.setText(currencyFormatter.format(item.getPrice()));
            binding.textViewProductLocation.setText(item.getAddressString() != null ? item.getAddressString() : "N/A");

            String imageUrl = (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) ? item.getImageUrls().get(0) : null;
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .centerCrop()
                    .into(binding.imageViewProduct);

            // Cập nhật trạng thái nút trái tim
            // Chỉ cần set trạng thái checked, XML sẽ tự lo phần còn lại
            binding.buttonFavorite.setChecked(isSaved);

            // Gán listeners
            itemView.setOnClickListener(v -> listener.onItemClick(item));
            binding.buttonFavorite.setOnClickListener(v -> listener.onFavoriteClick(item));
        }
    }

    static class HorizontalProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardHorizontalBinding binding;

        HorizontalProductViewHolder(ItemProductCardHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Item item, final OnProductClickListener listener) {
            binding.textViewFeaturedProductName.setText(item.getTitle());
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            binding.textViewFeaturedProductPrice.setText(currencyFormatter.format(item.getPrice()));

            String imageUrl = (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) ? item.getImageUrls().get(0) : null;
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .centerCrop()
                    .into(binding.imageViewFeaturedProduct);

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getItemId().equals(newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    Objects.equals(oldItem.getAddressString(), newItem.getAddressString()) &&
                    Objects.equals(oldItem.getImageUrls(), newItem.getImageUrls());
        }
    };
}