package com.example.tradeup.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.ItemMyListingBinding; // Tái sử dụng layout này

import java.text.NumberFormat;
import java.util.Locale;

public class SoldListingAdapter extends ListAdapter<Transaction, SoldListingAdapter.SoldViewHolder> {

    private final OnTransactionActionListener listener;

    public interface OnTransactionActionListener {
        void onRateBuyerClick(Transaction transaction);
        void onItemClick(String itemId); // Chỉ cần itemId để xem chi tiết
    }

    public SoldListingAdapter(@NonNull OnTransactionActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SoldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingBinding binding = ItemMyListingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SoldViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SoldViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class SoldViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingBinding binding;

        SoldViewHolder(ItemMyListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Transaction transaction, final OnTransactionActionListener listener) {
            binding.textViewProductName.setText(transaction.getItemTitle());

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            binding.textViewProductPrice.setText(currencyFormat.format(transaction.getPriceSold()));
            binding.chipProductStatus.setText("Sold");

            Glide.with(itemView.getContext())
                    .load(transaction.getItemImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(binding.imageViewProduct);

            // Ẩn các nút không cần thiết
            binding.buttonMenu.setVisibility(View.GONE);
            binding.textViewViews.setVisibility(View.GONE);

            // Hiển thị nút "Rate Buyer" nếu chưa đánh giá
            if (!transaction.isRatingGivenBySeller()) {
                binding.buttonRateBuyer.setVisibility(View.VISIBLE);
                binding.buttonRateBuyer.setOnClickListener(v -> listener.onRateBuyerClick(transaction));
            } else {
                binding.buttonRateBuyer.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(transaction.getItemId()));
        }
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getTransactionId().equals(newItem.getTransactionId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.isRatingGivenBySeller() == newItem.isRatingGivenBySeller();
        }
    };
}