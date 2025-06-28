// File: src/main/java/com/example/tradeup/ui/adapters/ReviewAdapter.java
package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Rating;
import com.example.tradeup.databinding.ItemReviewCardBinding;

public class ReviewAdapter extends ListAdapter<Rating, ReviewAdapter.ReviewViewHolder> {

    public ReviewAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Rating> DIFF_CALLBACK = new DiffUtil.ItemCallback<Rating>() {
        @Override
        public boolean areItemsTheSame(@NonNull Rating oldItem, @NonNull Rating newItem) {
            // === FIX: Sử dụng đúng ID ===
            return oldItem.getRatingId().equals(newItem.getRatingId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Rating oldItem, @NonNull Rating newItem) {
            // Giả sử Rating không có equals(), so sánh nội dung cơ bản
            return oldItem.getStars() == newItem.getStars() &&
                    oldItem.getFeedbackText().equals(newItem.getFeedbackText());
        }
    };

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewCardBinding binding = ItemReviewCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Rating rating = getItem(position);
        if (rating != null) {
            holder.bind(rating);
        }
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemReviewCardBinding binding;

        public ReviewViewHolder(ItemReviewCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Rating rating) {
            // === FIX: SỬ DỤNG ĐÚNG TÊN GETTER TỪ RATING MODEL ===
            binding.textViewRaterName.setText(rating.getRaterDisplayName());
            binding.textViewReviewDate.setText(rating.getFormattedDate()); // Sử dụng hàm tiện ích mới
            binding.ratingBarReview.setRating((float) rating.getStars());
            binding.textViewFeedbackText.setText(rating.getFeedbackText());

            Glide.with(itemView.getContext())
                    .load(rating.getRaterProfilePictureUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.imageViewRaterAvatar);
        }
    }
}