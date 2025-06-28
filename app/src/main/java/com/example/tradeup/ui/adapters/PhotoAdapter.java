// package: com.example.tradeup.ui.adapters
package com.example.tradeup.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.databinding.ItemAddPhotoPlaceholderBinding;
import com.example.tradeup.databinding.ItemSelectedPhotoBinding;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ADD = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int MAX_IMAGES = 10; // Giới hạn số lượng ảnh

    private final List<Uri> imageUris = new ArrayList<>();
    private final OnPhotoActionsListener listener;

    public interface OnPhotoActionsListener {
        void onAddPhotoClick();
        void onRemovePhotoClick(int position);
    }

    public PhotoAdapter(OnPhotoActionsListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        // Nếu vị trí cuối cùng và chưa đạt giới hạn ảnh -> hiển thị nút Add
        if (position == imageUris.size() && imageUris.size() < MAX_IMAGES) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ADD) {
            ItemAddPhotoPlaceholderBinding binding = ItemAddPhotoPlaceholderBinding.inflate(inflater, parent, false);
            return new AddViewHolder(binding);
        } else {
            ItemSelectedPhotoBinding binding = ItemSelectedPhotoBinding.inflate(inflater, parent, false);
            return new ImageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ADD) {
            ((AddViewHolder) holder).bind(listener);
        } else {
            ((ImageViewHolder) holder).bind(imageUris.get(position), position, listener);
        }
    }

    @Override
    public int getItemCount() {
        // Nếu chưa đủ ảnh thì +1 cho nút Add
        return imageUris.size() < MAX_IMAGES ? imageUris.size() + 1 : imageUris.size();
    }

    public void addImages(List<Uri> uris) {
        int oldSize = imageUris.size();
        for (Uri uri : uris) {
            if (imageUris.size() < MAX_IMAGES) {
                imageUris.add(uri);
            }
        }
        notifyItemRangeInserted(oldSize, imageUris.size() - oldSize);
        // Nếu đạt giới hạn, nút Add sẽ biến mất, cần notify
        if (imageUris.size() == MAX_IMAGES) {
            notifyItemChanged(MAX_IMAGES - 1);
        }
    }

    public void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            boolean wasAtMax = imageUris.size() == MAX_IMAGES;
            imageUris.remove(position);
            notifyItemRemoved(position);
            // Nếu vừa xóa ảnh khỏi trạng thái đầy, nút Add sẽ xuất hiện lại
            if (wasAtMax) {
                notifyItemInserted(imageUris.size());
            }
        }
    }

    public List<Uri> getImageUris() {
        return imageUris;
    }

    // ViewHolder cho nút "Thêm"
    static class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(ItemAddPhotoPlaceholderBinding binding) {
            super(binding.getRoot());
        }
        void bind(final OnPhotoActionsListener listener) {
            itemView.setOnClickListener(v -> listener.onAddPhotoClick());
        }
    }

    // ViewHolder cho ảnh đã chọn
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemSelectedPhotoBinding binding;
        ImageViewHolder(ItemSelectedPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(Uri uri, final int position, final OnPhotoActionsListener listener) {
            Glide.with(itemView.getContext()).load(uri).into(binding.imageViewPhoto);
            binding.buttonRemovePhoto.setOnClickListener(v -> listener.onRemovePhotoClick(position));
        }
    }
}