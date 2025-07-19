// File: src/main/java/com/example/tradeup/ui/admin/AdminItemAdapter.java
package com.example.tradeup.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.ItemAdminProductBinding;

public class AdminItemAdapter extends ListAdapter<Item, AdminItemAdapter.ItemViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClicked(Item item);
    }

    public AdminItemAdapter(@NonNull OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProductBinding binding = ItemAdminProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminProductBinding binding;
        private final OnItemClickListener listener;
        private final Context context;

        ItemViewHolder(ItemAdminProductBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.context = itemView.getContext();
        }

        void bind(final Item item) {
            binding.textViewTitle.setText(item.getTitle());
            binding.textViewSeller.setText("by " + item.getSellerDisplayName());

            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(item.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(binding.imageViewProduct);
            }

            // Cập nhật trạng thái và màu sắc
            String status = item.getStatus();
            binding.chipStatus.setText(status);
            switch (status.toLowerCase()) {
                case "available":
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_active_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_active_text));
                    break;
                case "sold":
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_sold_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_sold_text));
                    break;
                case "paused":
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_paused_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_paused_text));
                    break;
                default:
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_default_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_default_text));
                    break;
            }

            itemView.setOnClickListener(v -> listener.onItemClicked(item));
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getItemId().equals(newItem.getItemId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getStatus().equals(newItem.getStatus());
        }
    };
}