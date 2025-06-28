// package: com.example.tradeup.ui.adapters
package com.example.tradeup.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.databinding.ItemAddListingImageBinding;
import com.example.tradeup.databinding.ItemAddPhotoPlaceholderBinding;
import java.util.ArrayList;
import java.util.List;

public class AddItemImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ADD = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int MAX_IMAGES = 10;

    private List<Uri> imageUris = new ArrayList<>();
    private final OnImageActionsListener listener;

    public interface OnImageActionsListener {
        void onAddImageClick();
        void onRemoveImageClick(int position);
    }

    public AddItemImageAdapter(OnImageActionsListener listener) {
        this.listener = listener;
    }

    public void setImageUris(List<Uri> uris) {
        this.imageUris = new ArrayList<>(uris);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < imageUris.size()) {
            return VIEW_TYPE_IMAGE;
        }
        return VIEW_TYPE_ADD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_IMAGE) {
            return new ImageViewHolder(ItemAddListingImageBinding.inflate(inflater, parent, false));
        } else {
            return new AddViewHolder(ItemAddPhotoPlaceholderBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE) {
            ((ImageViewHolder) holder).bind(imageUris.get(position));
        }
        // AddViewHolder không cần bind dữ liệu
    }

    @Override
    public int getItemCount() {
        if (imageUris.size() < MAX_IMAGES) {
            return imageUris.size() + 1; // +1 cho nút "Add"
        }
        return imageUris.size(); // Đạt giới hạn, không hiện nút "Add"
    }

    // ViewHolder cho ảnh đã chọn
    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddListingImageBinding binding;
        ImageViewHolder(ItemAddListingImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.buttonRemoveImage.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRemoveImageClick(position);
                }
            });
        }
        void bind(Uri uri) {
            Glide.with(itemView.getContext()).load(uri).into(binding.imagePreview);
        }
    }

    // ViewHolder cho nút "Thêm ảnh"
    class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(ItemAddPhotoPlaceholderBinding binding) {
            super(binding.getRoot());
            binding.containerAddPhoto.setOnClickListener(v -> listener.onAddImageClick());
        }
    }
}