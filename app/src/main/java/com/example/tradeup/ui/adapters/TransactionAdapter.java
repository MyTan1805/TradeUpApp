package com.example.tradeup.ui.adapters;

import android.content.Context;
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
import com.example.tradeup.databinding.ItemTransactionBinding; // <-- Đảm bảo bạn đã có layout này

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    private final OnTransactionActionListener listener;
    private final String currentUserId;

    public interface OnTransactionActionListener {
        void onTransactionClick(Transaction transaction);
        void onRateClick(Transaction transaction);
    }

    public TransactionAdapter(@NonNull String currentUserId, @NonNull OnTransactionActionListener listener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TransactionViewHolder(binding, listener, currentUserId);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        if (transaction != null) {
            holder.bind(transaction);
        }
    }

    // ViewHolder
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;
        private final OnTransactionActionListener listener;
        private final String currentUserId;
        private final Context context;

        TransactionViewHolder(ItemTransactionBinding binding, OnTransactionActionListener listener, String currentUserId) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.currentUserId = currentUserId;
            this.context = itemView.getContext();
        }

        void bind(final Transaction transaction) {
            // Bind thông tin cơ bản
            binding.textViewProductName.setText(transaction.getItemTitle());
            binding.textViewTransactionId.setText("#" + transaction.getTransactionId().substring(0, 7)); // Hiển thị 7 ký tự đầu cho gọn

            // Format tiền tệ
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewPrice.setText(currencyFormat.format(transaction.getPriceSold()));

            // Format ngày tháng
            if (transaction.getTransactionDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                binding.textViewDate.setText(sdf.format(transaction.getTransactionDate().toDate()));
            } else {
                binding.textViewDate.setText("");
            }

            // Tải ảnh sản phẩm
            Glide.with(context)
                    .load(transaction.getItemImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_image_not_found)
                    .into(binding.imageViewProduct);

            // Xác định vai trò của người dùng và hiển thị thông tin đối tác
            boolean isUserTheBuyer = transaction.getBuyerId().equals(currentUserId);
            if (isUserTheBuyer) {
                binding.labelTransactionPartner.setText("Sold by:");
                // TODO: Cần có cơ chế lấy tên người bán từ sellerId
                binding.textViewUserName.setText(transaction.getSellerId().substring(0, 10) + "..."); // Tạm thời
            } else {
                binding.labelTransactionPartner.setText("Bought by:");
                // TODO: Cần có cơ chế lấy tên người mua từ buyerId
                binding.textViewUserName.setText(transaction.getBuyerId().substring(0, 10) + "..."); // Tạm thời
            }

            // Ẩn/Hiện nút "Rate"
            // TODO: Sửa lại tên button trong layout `item_transaction.xml` nếu cần
            /*
            boolean canUserRate = (isUserTheBuyer && !transaction.isRatingGivenByBuyer()) ||
                                  (!isUserTheBuyer && !transaction.isRatingGivenBySeller());
            binding.buttonRate.setVisibility(canUserRate ? View.VISIBLE : View.GONE);
            binding.buttonRate.setOnClickListener(v -> listener.onRateClick(transaction));
            */

            // Xử lý sự kiện click vào cả item
            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
        }
    }

    // DiffUtil.ItemCallback
    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getTransactionId().equals(newItem.getTransactionId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            // So sánh các trường quan trọng có thể thay đổi
            return oldItem.isRatingGivenByBuyer() == newItem.isRatingGivenByBuyer() &&
                    oldItem.isRatingGivenBySeller() == newItem.isRatingGivenBySeller();
        }
    };
}