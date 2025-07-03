package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.databinding.ItemOfferBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class OfferAdapter extends ListAdapter<Offer, OfferAdapter.OfferViewHolder> {

    private final OnOfferActionListener listener;
    private final String currentUserId;

    public interface OnOfferActionListener {
        void onAcceptClick(Offer offer);
        void onRejectClick(Offer offer);
        void onCounterClick(Offer offer);
        void onItemClick(Offer offer);
    }

    public OfferAdapter(@NonNull String currentUserId, @NonNull OnOfferActionListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOfferBinding binding = ItemOfferBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OfferViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        holder.bind(getItem(position), currentUserId, listener);
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfferBinding binding;
        private final Context context;

        OfferViewHolder(ItemOfferBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = itemView.getContext();
        }

        void bind(final Offer offer, final String currentUserId, final OnOfferActionListener listener) {
            binding.textViewProductName.setText("Item: " + offer.getItemId().substring(0, 6) + "...");

            if (offer.getCreatedAt() != null) {
                binding.textViewTimestamp.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(offer.getCreatedAt().toDate()));
            }

            // Hiển thị counterOfferPrice nếu có, nếu không thì dùng offeredPrice
            double displayPrice = offer.getCounterOfferPrice() != null ? offer.getCounterOfferPrice() : offer.getOfferedPrice();
            binding.textViewOfferPrice.setText(formatCurrency(displayPrice));

            updateUIBasedOnState(offer, currentUserId);

            itemView.setOnClickListener(v -> listener.onItemClick(offer));
            binding.buttonAccept.setOnClickListener(v -> listener.onAcceptClick(offer));
            binding.buttonReject.setOnClickListener(v -> listener.onRejectClick(offer));
            binding.buttonCounter.setOnClickListener(v -> listener.onCounterClick(offer));
        }

        private void updateUIBasedOnState(Offer offer, String currentUserId) {
            boolean isUserTheSeller = offer.getSellerId().equals(currentUserId);
            String status = offer.getStatus();

            // Kiểm tra xem có phải lượt của người dùng hay không
            boolean isMyTurn = "pending".equalsIgnoreCase(status) &&
                    (isUserTheSeller
                            ? offer.getCounterOfferPrice() == null // Seller chỉ chấp nhận offer ban đầu từ buyer
                            : offer.getCounterOfferPrice() != null); // Buyer chỉ chấp nhận counter-offer từ seller

            if (isUserTheSeller) {
                binding.textViewUserName.setText("Offer from: " + offer.getBuyerDisplayName());
            } else {
                binding.textViewUserName.setText("Offer to: " + (offer.getSellerDisplayName() != null ? offer.getSellerDisplayName() : "Unknown Seller"));
            }

            binding.chipStatus.setText(status);
            setChipStyle(status);

            if (isMyTurn) {
                binding.layoutActionButtons.setVisibility(View.VISIBLE);
                binding.buttonAccept.setText(isUserTheSeller ? "Accept & Sell" : "Accept Price");
            } else {
                binding.layoutActionButtons.setVisibility(View.GONE);
            }
        }

        private void setChipStyle(String status) {
            int bgColorRes;
            int textColorRes;
            switch (status.toLowerCase()) {
                case "pending":
                    bgColorRes = R.color.status_pending_background;
                    textColorRes = R.color.status_pending_text;
                    break;
                case "accepted":
                    bgColorRes = R.color.status_completed_background;
                    textColorRes = R.color.status_completed_text;
                    break;
                default: // rejected, cancelled, expired
                    bgColorRes = R.color.status_sold_background;
                    textColorRes = R.color.status_sold_text;
                    break;
            }
            binding.chipStatus.setChipBackgroundColorResource(bgColorRes);
            binding.chipStatus.setTextColor(ContextCompat.getColor(context, textColorRes));
        }

        private String formatCurrency(double price) {
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(price);
        }
    }

    private static final DiffUtil.ItemCallback<Offer> DIFF_CALLBACK = new DiffUtil.ItemCallback<Offer>() {
        @Override
        public boolean areItemsTheSame(@NonNull Offer oldItem, @NonNull Offer newItem) {
            return oldItem.getOfferId().equals(newItem.getOfferId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Offer oldItem, @NonNull Offer newItem) {
            return oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getOfferedPrice() == newItem.getOfferedPrice() &&
                    Objects.equals(oldItem.getCounterOfferPrice(), newItem.getCounterOfferPrice()) &&
                    Objects.equals(oldItem.getCounterOfferMessage(), newItem.getCounterOfferMessage());
        }
    };
}