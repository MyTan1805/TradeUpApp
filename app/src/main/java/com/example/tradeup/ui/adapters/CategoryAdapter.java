// package: com.example.tradeup.ui.adapters
package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.databinding.ItemCategoryChipBinding;

public class CategoryAdapter extends ListAdapter<DisplayCategoryConfig, CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(DisplayCategoryConfig category);
    }

    private final OnCategoryClickListener listener;

    public CategoryAdapter(OnCategoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryChipBinding binding = ItemCategoryChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        DisplayCategoryConfig category = getItem(position);
        holder.bind(category, listener);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryChipBinding binding;

        CategoryViewHolder(ItemCategoryChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final DisplayCategoryConfig category, final OnCategoryClickListener listener) {
            binding.textViewCategoryName.setText(category.getName());

            Glide.with(itemView.getContext())
                    .load(category.getIconUrl()) // Glide sẽ tự xử lý nếu URL là null
                    .placeholder(R.drawable.ic_category) // Một icon placeholder chung
                    .into(binding.imageViewCategoryIcon);

            itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        }
    }

    private static final DiffUtil.ItemCallback<DisplayCategoryConfig> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DisplayCategoryConfig>() {
                @Override
                public boolean areItemsTheSame(@NonNull DisplayCategoryConfig oldItem, @NonNull DisplayCategoryConfig newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DisplayCategoryConfig oldItem, @NonNull DisplayCategoryConfig newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            (oldItem.getIconUrl() != null ? oldItem.getIconUrl().equals(newItem.getIconUrl()) : newItem.getIconUrl() == null);
                }
            };
}