// File: src/main/java/com/example/tradeup/ui/adapters/TransactionAdapter.java
// << PHIÊN BẢN CUỐI CÙNG, HOÀN CHỈNH >>

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
import com.example.tradeup.databinding.ItemTransactionBinding; // Đảm bảo tên này đúng
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
        void onMarkAsShippedClick(Transaction transaction);
        void onConfirmReceiptClick(Transaction transaction);
        void onProceedToPaymentClick(Transaction transaction);
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
            // --- 1. BIND THÔNG TIN CƠ BẢN ---
            binding.textViewProductName.setText(transaction.getItemTitle());

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.textViewPrice.setText(currencyFormat.format(transaction.getPriceSold()));

            if (transaction.getTransactionDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                binding.textViewDate.setText(sdf.format(transaction.getTransactionDate().toDate()));
            } else {
                binding.textViewDate.setText("Pending");
            }

            Glide.with(context)
                    .load(transaction.getItemImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_image_not_found)
                    .into(binding.imageViewProduct);

            boolean isUserTheBuyer = transaction.getBuyerId().equals(currentUserId);
            if (isUserTheBuyer) {
                binding.labelTransactionPartner.setText("Sold by:");
                fetchUserDisplayName(transaction.getSellerId(), binding.textViewUserName);
            } else {
                binding.labelTransactionPartner.setText("Bought by:");
                fetchUserDisplayName(transaction.getBuyerId(), binding.textViewUserName);
            }

            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));

            // --- 2. ẨN NÚT HÀNH ĐỘNG ---
            binding.buttonAction.setVisibility(View.GONE);

            // --- 3. XỬ LÝ LOGIC HIỂN THỊ ---
            String paymentStatus = transaction.getPaymentStatus() != null ? transaction.getPaymentStatus().toLowerCase() : "pending";
            String shippingStatus = transaction.getShippingStatus();
            String paymentMethod = transaction.getPaymentMethod();

            if ("completed".equals(paymentStatus)) {
                binding.chipStatus.setText("Completed");
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_completed_background);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text));

                boolean canUserRate = (isUserTheBuyer && !transaction.isRatingGivenByBuyer()) || (!isUserTheBuyer && !transaction.isRatingGivenBySeller());
                if (canUserRate) {
                    binding.buttonAction.setText("Rate Transaction");
                    binding.buttonAction.setVisibility(View.VISIBLE);
                    binding.buttonAction.setOnClickListener(v -> listener.onRateClick(transaction));
                }
            } else if ("pending".equals(paymentStatus)) {
                if (paymentMethod == null) {
                    binding.chipStatus.setText("Pending Payment");
                    if (isUserTheBuyer) {
                        binding.buttonAction.setText("Proceed to Payment");
                        binding.buttonAction.setVisibility(View.VISIBLE);
                        binding.buttonAction.setOnClickListener(v -> listener.onProceedToPaymentClick(transaction));
                    }
                } else if ("COD".equalsIgnoreCase(paymentMethod)) {
                    if ("waiting_for_shipment".equals(shippingStatus)) {
                        binding.chipStatus.setText("Waiting for Shipment");
                        if (!isUserTheBuyer) {
                            binding.buttonAction.setText("Mark as Shipped");
                            binding.buttonAction.setVisibility(View.VISIBLE);
                            binding.buttonAction.setOnClickListener(v -> listener.onMarkAsShippedClick(transaction));
                        }
                    } else if ("shipped".equals(shippingStatus)) {
                        binding.chipStatus.setText("In Transit");
                        if (isUserTheBuyer) {
                            binding.buttonAction.setText("Confirm Receipt");
                            binding.buttonAction.setVisibility(View.VISIBLE);
                            binding.buttonAction.setOnClickListener(v -> listener.onConfirmReceiptClick(transaction));
                        }
                    }
                } else if ("Online".equalsIgnoreCase(paymentMethod)) {
                    binding.chipStatus.setText("Processing Payment");
                }
            }
        }

        // << HÀM BỊ THIẾU ĐÃ ĐƯỢC THÊM VÀO ĐÂY >>
        private void fetchUserDisplayName(String userId, TextView textView) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String displayName = documentSnapshot.getString("displayName");
                            textView.setText(displayName != null ? displayName : "A user");
                        } else {
                            textView.setText("A user");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textView.setText("A user");
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
            // So sánh các trường có khả năng thay đổi để cập nhật UI
            return oldItem.isRatingGivenByBuyer() == newItem.isRatingGivenByBuyer() &&
                    oldItem.isRatingGivenBySeller() == newItem.isRatingGivenBySeller() &&
                    Objects.equals(oldItem.getPaymentStatus(), newItem.getPaymentStatus()) &&
                    Objects.equals(oldItem.getShippingStatus(), newItem.getShippingStatus());
        }
    };
}