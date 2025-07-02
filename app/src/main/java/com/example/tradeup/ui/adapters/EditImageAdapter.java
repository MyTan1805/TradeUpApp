package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.databinding.ItemAddPhotoPlaceholderBinding;
import com.example.tradeup.databinding.ItemEditPhotoBinding;
import com.example.tradeup.ui.listing.ImageSource;

public class EditImageAdapter extends ListAdapter<ImageSource, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ADD = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int MAX_IMAGES = 10;

    private final OnImageActionsListener listener;

    public interface OnImageActionsListener {
        void onAddImageClick();
        void onRemoveImageClick(ImageSource imageSource);
    }

    public EditImageAdapter(OnImageActionsListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        // Nếu vị trí nằm ngoài danh sách -> đó là nút Add
        if (position == getItemCount() - 1 && getItemCount() <= MAX_IMAGES) {
            // Kiểm tra xem thực sự có item nào không, nếu không thì item duy nhất là nút add
            if (getCurrentList().isEmpty() || position >= getCurrentList().size()) {
                return VIEW_TYPE_ADD;
            }
        }
        return VIEW_TYPE_IMAGE;
    }

    @Override
    public int getItemCount() {
        int listSize = getCurrentList().size();
        return listSize < MAX_IMAGES ? listSize + 1 : listSize;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_IMAGE) {
            return new ImageViewHolder(ItemEditPhotoBinding.inflate(inflater, parent, false));
        } else {
            return new AddViewHolder(ItemAddPhotoPlaceholderBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_IMAGE) {
            ((ImageViewHolder) holder).bind(getItem(position), listener);
        } else if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind(listener);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemEditPhotoBinding binding;
        ImageViewHolder(ItemEditPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final ImageSource imageSource, final OnImageActionsListener listener) {
            Object source = (imageSource instanceof ImageSource.ExistingUrl)
                    ? ((ImageSource.ExistingUrl) imageSource).url
                    : ((ImageSource.NewUri) imageSource).uri;

            Glide.with(itemView.getContext()).load(source).into(binding.imageViewPhoto);
            binding.buttonRemoveImage.setOnClickListener(v -> listener.onRemoveImageClick(imageSource));
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        AddViewHolder(ItemAddPhotoPlaceholderBinding binding) {
            super(binding.getRoot());
        }
        void bind(final OnImageActionsListener listener) {
            itemView.setOnClickListener(v -> listener.onAddImageClick());
        }
    }

    private static final DiffUtil.ItemCallback<ImageSource> DIFF_CALLBACK = new DiffUtil.ItemCallback<ImageSource>() {
        @Override
        public boolean areItemsTheSame(@NonNull ImageSource oldItem, @NonNull ImageSource newItem) {
            if (oldItem instanceof ImageSource.ExistingUrl && newItem instanceof ImageSource.ExistingUrl) {
                return ((ImageSource.ExistingUrl) oldItem).url.equals(((ImageSource.ExistingUrl) newItem).url);
            }
            if (oldItem instanceof ImageSource.NewUri && newItem instanceof ImageSource.NewUri) {
                return ((ImageSource.NewUri) oldItem).uri.equals(((ImageSource.NewUri) newItem).uri);
            }
            return false;
        }
        @Override
        public boolean areContentsTheSame(@NonNull ImageSource oldItem, @NonNull ImageSource newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    };
}