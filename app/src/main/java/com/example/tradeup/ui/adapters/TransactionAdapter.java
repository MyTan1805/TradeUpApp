package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.ItemTransactionBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    private final OnTransactionActionListener listener;
    private final String currentUserId;

    public interface OnTransactionActionListener {
        void onTransactionClick(Transaction transaction);
        void onRateClick(Transaction transaction);
        void onConfirmCOD(Transaction transaction);
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
            binding.textViewTransactionId.setText("#" + transaction.getTransactionId().substring(0, 7));

            // Format tiền tệ
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewPrice.setText(currencyFormat.format(transaction.getPriceSold()));

            // Hiển thị paymentMethod và paymentStatus
            binding.textViewPaymentMethod.setText("Payment Method: " + (transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "Not selected"));
            binding.textViewPaymentStatus.setText("Status: " + (transaction.getPaymentStatus() != null ? transaction.getPaymentStatus() : "Pending"));

            // Format ngày tháng
            if (transaction.getTransactionDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                binding.textViewDate.setText(sdf.format(transaction.getTransactionDate().toDate()));
            } else {
                binding.textViewDate.setText("Pending");
            }

            // Tải ảnh sản phẩm
            Glide.with(context)
                    .load(transaction.getItemImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_image_not_found)
                    .into(binding.imageViewProduct);

            // Xác định vai trò và lấy displayName từ Firestore
            boolean isUserTheBuyer = transaction.getBuyerId().equals(currentUserId);
            if (isUserTheBuyer) {
                binding.labelTransactionPartner.setText("Sold by:");
                fetchUserDisplayName(transaction.getSellerId(), binding.textViewUserName);
            } else {
                binding.labelTransactionPartner.setText("Bought by:");
                fetchUserDisplayName(transaction.getBuyerId(), binding.textViewUserName);
            }

            // Cập nhật chipStatus dựa trên paymentStatus
            String status = transaction.getPaymentStatus() != null ? transaction.getPaymentStatus() : "pending";
            binding.chipStatus.setText(status);
            int backgroundColorRes;
            int textColorRes;
            switch (status.toLowerCase()) {
                case "pending":
                    backgroundColorRes = R.color.status_pending_background;
                    textColorRes = R.color.status_pending_text;
                    break;
                case "completed":
                    backgroundColorRes = R.color.status_completed_background;
                    textColorRes = R.color.status_completed_text;
                    break;
                default:
                    backgroundColorRes = R.color.status_default_background;
                    textColorRes = R.color.status_default_text;
                    break;
            }
            binding.chipStatus.setChipBackgroundColorResource(backgroundColorRes);
            binding.chipStatus.setTextColor(ContextCompat.getColor(context, textColorRes));

            // Hiển thị nút Rate
            boolean canUserRate = (isUserTheBuyer && !transaction.isRatingGivenByBuyer()) ||
                    (!isUserTheBuyer && !transaction.isRatingGivenBySeller());
            binding.buttonRate.setVisibility(canUserRate ? View.VISIBLE : View.GONE);
            binding.buttonRate.setOnClickListener(v -> listener.onRateClick(transaction));

            // Hiển thị nút Confirm COD Payment cho seller
            boolean isCODPending = "COD".equals(transaction.getPaymentMethod()) && "pending".equals(transaction.getPaymentStatus());
            binding.buttonConfirmCOD.setVisibility(!isUserTheBuyer && isCODPending ? View.VISIBLE : View.GONE);
            binding.buttonConfirmCOD.setOnClickListener(v -> listener.onConfirmCOD(transaction));

            // Xử lý sự kiện click vào cả item
            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
        }

        private void fetchUserDisplayName(String userId, TextView textView) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String displayName = documentSnapshot.getString("displayName");
                        textView.setText(displayName != null ? displayName : userId.substring(0, 10) + "...");
                    })
                    .addOnFailureListener(e -> {
                        textView.setText(userId.substring(0, 10) + "...");
                    });
        }
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getTransactionId().equals(newItem.getTransactionId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.isRatingGivenByBuyer() == newItem.isRatingGivenByBuyer() &&
                    oldItem.isRatingGivenBySeller() == newItem.isRatingGivenBySeller() &&
                    Objects.equals(oldItem.getPaymentMethod(), newItem.getPaymentMethod()) &&
                    Objects.equals(oldItem.getPaymentStatus(), newItem.getPaymentStatus());
        }
    };
}