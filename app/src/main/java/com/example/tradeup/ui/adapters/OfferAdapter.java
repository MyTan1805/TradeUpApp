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

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.databinding.ItemOfferBinding; // Bạn cần tạo layout này

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OfferAdapter extends ListAdapter<Offer, OfferAdapter.OfferViewHolder> {

    private final OnOfferActionListener listener;
    private final boolean isReceivedOffers; // Biến để xác định đây là tab "Received" hay "Sent"

    // Interface để fragment lắng nghe sự kiện từ các nút
    public interface OnOfferActionListener {
        void onAccept(Offer offer);
        void onReject(Offer offer);
        void onCounter(Offer offer);
        void onItemClick(Offer offer); // Click vào cả item
    }

    public OfferAdapter(boolean isReceivedOffers, @NonNull OnOfferActionListener listener) {
        super(DIFF_CALLBACK);
        this.isReceivedOffers = isReceivedOffers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOfferBinding binding = ItemOfferBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OfferViewHolder(binding, isReceivedOffers, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfferBinding binding;
        private final boolean isReceivedOffers;
        private final OnOfferActionListener listener;
        private final Context context;

        OfferViewHolder(ItemOfferBinding binding, boolean isReceivedOffers, OnOfferActionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.isReceivedOffers = isReceivedOffers;
            this.listener = listener;
            this.context = itemView.getContext();
        }

        void bind(Offer offer) {
            // Bind dữ liệu chung
            // TODO: Cần lấy thông tin sản phẩm từ ItemRepository, hiện tại dùng thông tin denormalized
            binding.textViewProductName.setText(offer.getItemId()); // Tạm thời
            binding.textViewOfferPrice.setText(formatCurrency(offer.getOfferedPrice()));
            binding.textViewUserName.setText(isReceivedOffers ? offer.getBuyerDisplayName() : "Your Offer");

            if (offer.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                binding.textViewTimestamp.setText(sdf.format(offer.getCreatedAt().toDate()));
            }

            // Bind trạng thái và các nút hành động
            updateStatus(offer);

            // Gán sự kiện click
            binding.buttonAccept.setOnClickListener(v -> listener.onAccept(offer));
            binding.buttonReject.setOnClickListener(v -> listener.onReject(offer));
            binding.buttonCounter.setOnClickListener(v -> listener.onCounter(offer));
            itemView.setOnClickListener(v -> listener.onItemClick(offer));
        }

        private void updateStatus(Offer offer) {
            String status = offer.getStatus();
            binding.chipStatus.setText(status);

            // Mặc định ẩn hết các nút
            binding.layoutActionButtons.setVisibility(View.GONE);

            // Logic hiển thị nút và màu sắc chip
            if ("pending".equalsIgnoreCase(status)) {
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_pending_background);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                // Chỉ hiển thị nút cho người nhận offer
                if (isReceivedOffers) {
                    binding.layoutActionButtons.setVisibility(View.VISIBLE);
                }
            } else if ("accepted".equalsIgnoreCase(status)) {
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_completed_background);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text));
            } else { // Rejected, countered, etc.
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_sold_background);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_sold_text));
            }
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
            return oldItem.getStatus().equals(newItem.getStatus()) && oldItem.getOfferedPrice() == newItem.getOfferedPrice();
        }
    };
}