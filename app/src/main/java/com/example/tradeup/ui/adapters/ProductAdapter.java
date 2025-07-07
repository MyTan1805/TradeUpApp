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
import java.util.Locale;
import java.util.Objects;

public class ProductAdapter extends ListAdapter<Item, RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_GRID = 1;
    public static final int VIEW_TYPE_HORIZONTAL = 2;

    private final int viewType;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onItemClick(Item item);
        void onFavoriteClick(Item item, boolean isCurrentlyFavorite);
    }

    public ProductAdapter(int viewType, OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.viewType = viewType;
        this.listener = listener;
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
        if (holder instanceof GridProductViewHolder) {
            ((GridProductViewHolder) holder).bind(currentItem, listener);
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

        void bind(final Item item, final OnProductClickListener listener) {
            binding.textViewProductName.setText(item.getTitle());
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewProductPrice.setText(currencyFormatter.format(item.getPrice()));

            String imageUrl = (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) ? item.getImageUrls().get(0) : null;
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(binding.imageViewProduct);

            // Sửa lỗi: Sử dụng getAddressString() trực tiếp từ Item
            binding.textViewProductLocation.setText(item.getAddressString() != null ? item.getAddressString() : "N/A");

            // TODO: Nếu thêm trường rating vào Item, cập nhật tại đây
            // binding.textViewProductRating.setText(item.getRating() != null ? String.format("%.1f", item.getRating()) : "N/A");

            // Giả sử có một phương thức isFavorite() trong Item hoặc từ dữ liệu khác
            boolean isFavorite = false; // TODO: Lấy trạng thái từ dữ liệu (ví dụ: Firestore hoặc Room)
            binding.buttonFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            binding.buttonFavorite.setOnClickListener(v -> {
                boolean newFavoriteState = !isFavorite;
                binding.buttonFavorite.setImageResource(newFavoriteState ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                listener.onFavoriteClick(item, newFavoriteState);
            });
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
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewFeaturedProductPrice.setText(currencyFormatter.format(item.getPrice()));

            String imageUrl = (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) ? item.getImageUrls().get(0) : null;
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(binding.imageViewFeaturedProduct);

            // Có thể thêm addressString nếu layout ngang cần
            // binding.textViewFeaturedProductLocation.setText(item.getAddressString() != null ? item.getAddressString() : "N/A");

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