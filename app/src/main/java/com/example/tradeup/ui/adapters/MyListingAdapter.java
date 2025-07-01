// File: src/main/java/com/example/tradeup/ui/adapters/MyListingAdapter.java

package com.example.tradeup.ui.adapters;

import android.content.Context;
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

    private final OnItemMenuClickListener listener;

    public interface OnItemMenuClickListener {
        void onMenuClick(Item item);
        void onRateBuyerClick(Item item);
    }

    public MyListingAdapter(@NonNull OnItemMenuClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingBinding binding = ItemMyListingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        Item currentItem = getItem(position);
        if (currentItem != null) {
            holder.bind(currentItem, listener);
        }
    }

    static class MyListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;

        MyListingViewHolder(ItemMyListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Item item, final OnItemMenuClickListener listener) {
            binding.textViewProductName.setText(item.getTitle());
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewProductPrice.setText(currencyFormat.format(item.getPrice()));

            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_image_not_found)
                        .into(binding.imageViewProduct);
            } else {
                binding.imageViewProduct.setImageResource(R.drawable.ic_placeholder_image);
            }

            binding.chipProductStatus.setText(item.getStatus());
            binding.textViewViews.setText(String.format(Locale.getDefault(), "%d views", item.getViewsCount()));

            binding.buttonMenu.setOnClickListener(v -> listener.onMenuClick(item));

            if ("sold".equalsIgnoreCase(item.getStatus())) {
                binding.buttonRateBuyer.setVisibility(View.VISIBLE);
                binding.buttonRateBuyer.setOnClickListener(v -> listener.onRateBuyerClick(item));
            } else {
                binding.buttonRateBuyer.setVisibility(View.GONE);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getItemId().equals(newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.equals(newItem);
        }
    };
}