// File: src/main/java/com/example/tradeup/ui/adapters/OfferAdapter.java
package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.databinding.ItemOfferBinding;
import com.example.tradeup.ui.offers.OfferViewData; // Import lớp mới

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class OfferAdapter extends ListAdapter<OfferViewData, OfferAdapter.OfferViewHolder> {

    private final String currentUserId;
    private final OnOfferActionListener listener;

    public interface OnOfferActionListener {
        void onAcceptClick(OfferViewData data);
        void onRejectClick(OfferViewData data);
        void onCounterClick(OfferViewData data);
        void onItemClick(OfferViewData data);
    }

    public OfferAdapter(String currentUserId, OnOfferActionListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOfferBinding binding = ItemOfferBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new OfferViewHolder(binding, listener, currentUserId);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfferBinding binding;
        private final OnOfferActionListener listener;
        private final String currentUserId;
        private final Context context;

        OfferViewHolder(ItemOfferBinding binding, OnOfferActionListener listener, String currentUserId) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.currentUserId = currentUserId;
            this.context = itemView.getContext();
        }

        void bind(final OfferViewData data) {
            if (data == null) return;
            final Offer offer = data.offer;
            final Item item = data.relatedItem;

            final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            final boolean isUserTheSeller = currentUserId.equals(offer.getSellerId());

            // Bind thông tin sản phẩm (nếu có)
            if (item != null) {
                binding.textViewProductName.setText(item.getTitle());
                binding.textViewOriginalPrice.setText(currencyFormat.format(item.getPrice()));
                if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(item.getImageUrls().get(0))
                            .placeholder(R.drawable.ic_placeholder_image)
                            .into(binding.imageViewProduct);
                } else {
                    binding.imageViewProduct.setImageResource(R.drawable.ic_placeholder_image);
                }
            } else {
                binding.textViewProductName.setText("[Item may have been deleted]");
                binding.textViewOriginalPrice.setText("");
                binding.imageViewProduct.setImageResource(R.drawable.ic_image_not_found);
            }

            // Bind thông tin offer
            binding.textViewUserName.setText(isUserTheSeller ? offer.getBuyerDisplayName() : "");
            binding.textViewUserName.setVisibility(isUserTheSeller ? View.VISIBLE : View.GONE);
            binding.textViewOfferPrice.setText(currencyFormat.format(offer.getCurrentPrice()));

            if (offer.getUpdatedAt() != null) {
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        offer.getUpdatedAt().toDate().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                binding.textViewTimestamp.setText(relativeTime);
            }

            // Gán listener, truyền cả OfferViewData
            binding.buttonAccept.setOnClickListener(v -> listener.onAcceptClick(data));
            binding.buttonReject.setOnClickListener(v -> listener.onRejectClick(data));
            binding.buttonCounter.setOnClickListener(v -> listener.onCounterClick(data));
            itemView.setOnClickListener(v -> listener.onItemClick(data));

            // Logic hiển thị nút và trạng thái
            binding.layoutActionButtons.setVisibility(View.GONE);
            binding.chipStatus.setVisibility(View.GONE);
            boolean isCurrentUserTheLastActor = currentUserId.equals(offer.getLastActionByUid());
            switch (offer.getStatus().toLowerCase()) {
                case "pending":
                    if (isCurrentUserTheLastActor) {
                        binding.chipStatus.setVisibility(View.VISIBLE);
                        binding.chipStatus.setText("Waiting for response");
                        binding.chipStatus.setChipBackgroundColorResource(R.color.status_pending_background);
                        binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                    } else {
                        binding.layoutActionButtons.setVisibility(View.VISIBLE);
                    }
                    break;
                case "accepted":
                    binding.chipStatus.setVisibility(View.VISIBLE);
                    binding.chipStatus.setText("Accepted");
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_completed_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text));
                    break;
                case "rejected":
                    binding.chipStatus.setVisibility(View.VISIBLE);
                    binding.chipStatus.setText("Rejected");
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_sold_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                    break;
                default:
                    binding.chipStatus.setVisibility(View.VISIBLE);
                    binding.chipStatus.setText(offer.getStatus());
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_default_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_default_text));
                    break;
            }
        }
    }

    private static final DiffUtil.ItemCallback<OfferViewData> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<OfferViewData>() {
                @Override
                public boolean areItemsTheSame(@NonNull OfferViewData oldItem, @NonNull OfferViewData newItem) {
                    return oldItem.offer.getOfferId().equals(newItem.offer.getOfferId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull OfferViewData oldItem, @NonNull OfferViewData newItem) {
                    return Objects.equals(oldItem.offer.getStatus(), newItem.offer.getStatus()) &&
                            oldItem.offer.getCurrentPrice() == newItem.offer.getCurrentPrice() &&
                            Objects.equals(oldItem.offer.getLastActionByUid(), newItem.offer.getLastActionByUid());
                }
            };
}