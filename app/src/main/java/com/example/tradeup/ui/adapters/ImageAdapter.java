// File: src/main/java/com/example/tradeup/ui/adapters/ImageAdapter.java

package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;

import java.util.function.Consumer;

public class ImageAdapter extends ListAdapter<String, ImageAdapter.ImageViewHolder> {
    private final Consumer<String> onRemoveImageClickListener;

    public ImageAdapter(Consumer<String> onRemoveImageClickListener) {
        super(DIFF_CALLBACK);
        this.onRemoveImageClickListener = onRemoveImageClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_photo, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = getItem(position);
        holder.bind(imageUrl);
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageButton removeButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPhoto);
            removeButton = itemView.findViewById(R.id.buttonRemoveImage);
        }

        void bind(String imageUrl) {
            // Sử dụng Glide để tải hình ảnh
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .centerCrop()
                    .into(imageView);

            // Xử lý sự kiện nhấn nút xóa
            removeButton.setOnClickListener(v -> {
                if (onRemoveImageClickListener != null) {
                    onRemoveImageClickListener.accept(imageUrl);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    };
}