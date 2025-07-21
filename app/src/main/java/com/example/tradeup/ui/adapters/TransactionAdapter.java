// File: src/main/java/com/example/tradeup/ui/adapters/TransactionAdapter.java
// << PHIÊN BẢN CUỐI CÙNG, ĐÃ SỬA LỖI LOGIC HIỂN THỊ >>

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
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.ItemTransactionBinding;
import com.example.tradeup.ui.profile.TransactionViewData;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class TransactionAdapter extends ListAdapter<TransactionViewData, TransactionAdapter.TransactionViewHolder> {

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
        TransactionViewData viewData = getItem(position);
        if (viewData != null) {
            holder.bind(viewData);
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

        void bind(final TransactionViewData viewData) {
            final Transaction transaction = viewData.transaction;
            final String partnerName = viewData.partnerName;

            // --- PHẦN 1: BIND DỮ LIỆU CƠ BẢN (KHÔNG ĐỔI) ---
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
            final boolean isUserTheBuyer = transaction.getBuyerId().equals(currentUserId);
            String label = isUserTheBuyer ? "Sold by:" : "Bought by:";
            binding.labelTransactionPartner.setText(label);
            binding.textViewUserName.setText(partnerName != null ? partnerName : "A user");
            itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));

            // --- PHẦN 2: LOGIC HIỂN THỊ TRẠNG THÁI (ĐÃ SẮP XẾP LẠI) ---
            binding.buttonAction.setVisibility(View.GONE);
            binding.buttonAction.setOnClickListener(null);

            String paymentStatus = transaction.getPaymentStatus() != null ? transaction.getPaymentStatus().toLowerCase() : "pending";
            String shippingStatus = transaction.getShippingStatus();
            String paymentMethod = transaction.getPaymentMethod();

            // KỊCH BẢN 1: GIAO DỊCH ĐÃ HOÀN TẤT
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
            }
            // KỊCH BẢN 2: THANH TOÁN ONLINE ĐÃ ĐƯỢC ỦY QUYỀN (ESCROW)
            else if ("paid".equals(paymentStatus)) {
                if ("waiting_for_shipment".equals(shippingStatus)) {
                    binding.chipStatus.setText("Waiting for Shipment");
                    if (!isUserTheBuyer) { // Người bán sẽ thấy nút này
                        binding.buttonAction.setText("Mark as Shipped");
                        binding.buttonAction.setVisibility(View.VISIBLE);
                        binding.buttonAction.setOnClickListener(v -> listener.onMarkAsShippedClick(transaction));
                    }
                } else if ("shipped".equals(shippingStatus)) {
                    binding.chipStatus.setText("In Transit");
                    if (isUserTheBuyer) { // Người mua sẽ thấy nút này
                        binding.buttonAction.setText("Confirm Receipt");
                        binding.buttonAction.setVisibility(View.VISIBLE);
                        binding.buttonAction.setOnClickListener(v -> listener.onConfirmReceiptClick(transaction));
                    }
                }
            }
            // KỊCH BẢN 3: GIAO DỊCH ĐANG CHỜ XỬ LÝ (CHƯA THANH TOÁN)
            else if ("pending".equals(paymentStatus)) {
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
                    } else {
                        binding.chipStatus.setText("COD Pending");
                    }
                } else if ("Online".equalsIgnoreCase(paymentMethod)) {
                    // Trạng thái này chỉ xuất hiện rất ngắn trước khi paymentStatus đổi thành "paid"
                    binding.chipStatus.setText("Processing Payment");
                }
            }
            // KỊCH BẢN 4: CÁC TRƯỜNG HỢP KHÁC (FAILED, REJECTED...)
            else {
                binding.chipStatus.setText(transaction.getPaymentStatus());
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_default_background);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_default_text));
            }
        }
    }

    // *** BƯỚC 5: CẬP NHẬT DIFF_CALLBACK ĐỂ LÀM VIỆC VỚI TransactionViewData ***
    private static final DiffUtil.ItemCallback<TransactionViewData> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionViewData>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionViewData oldItem, @NonNull TransactionViewData newItem) {
            // So sánh ID của transaction gốc
            return oldItem.transaction.getTransactionId().equals(newItem.transaction.getTransactionId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionViewData oldItem, @NonNull TransactionViewData newItem) {
            // So sánh tất cả các trường có thể ảnh hưởng đến UI
            Transaction oldT = oldItem.transaction;
            Transaction newT = newItem.transaction;
            return oldT.isRatingGivenByBuyer() == newT.isRatingGivenByBuyer() &&
                    oldT.isRatingGivenBySeller() == newT.isRatingGivenBySeller() &&
                    Objects.equals(oldT.getPaymentStatus(), newT.getPaymentStatus()) &&
                    Objects.equals(oldT.getShippingStatus(), newT.getShippingStatus()) &&
                    Objects.equals(oldItem.partnerName, newItem.partnerName); // So sánh cả tên đối tác
        }
    };
}