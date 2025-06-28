// package: com.example.tradeup.ui.adapters
// File: ProductAdapter.java (đã có từ trước, giờ cập nhật)
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

public class ProductAdapter extends ListAdapter<Item, RecyclerView.ViewHolder> {

    // Định nghĩa các view type
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
        } else { // Mặc định là GRID
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

    // ViewHolder cho layout lưới (item_product_card.xml)
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
            Glide.with(itemView.getContext()).load(imageUrl).placeholder(R.drawable.ic_placeholder_image).into(binding.imageViewProduct);

            // TODO: Lấy location và rating từ item. Cần thêm field này vào model Item.
            binding.textViewProductLocation.setText(item.getLocation() != null ? item.getLocation().getAddressString() : "N/A");
            // binding.textViewProductRating.setText("4.5"); // Cần dữ liệu rating

            // TODO: Cập nhật trạng thái nút yêu thích từ dữ liệu (ví dụ: isSaved)
            // binding.buttonFavorite.setImageResource(isSaved ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            binding.buttonFavorite.setOnClickListener(v -> {
                // TODO: Truyền trạng thái hiện tại của nút favorite
                listener.onFavoriteClick(item, false);
            });
        }
    }

    // ViewHolder cho layout ngang (item_product_card_horizontal.xml)
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
            Glide.with(itemView.getContext()).load(imageUrl).placeholder(R.drawable.ic_placeholder_image).into(binding.imageViewFeaturedProduct);

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
            return oldItem.getTitle().equals(newItem.getTitle()) && oldItem.getPrice() == newItem.getPrice();
        }
    };
}