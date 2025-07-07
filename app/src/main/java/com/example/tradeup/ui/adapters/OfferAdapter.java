// File: src/main/java/com/example/tradeup/ui/adapters/OfferAdapter.java
// << PHIÊN BẢN CUỐI CÙNG - ĐỒNG BỘ VỚI LAYOUT CỦA BẠN >>

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
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.databinding.ItemOfferBinding; // Tên binding của bạn có thể khác
import com.example.tradeup.core.utils.Callback;


import java.text.NumberFormat;
import java.util.Locale;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

public class OfferAdapter extends ListAdapter<Offer, OfferAdapter.OfferViewHolder> {

    private final String currentUserId;
    private final OnOfferActionListener listener;
    private final ItemRepository itemRepository;

    public interface OnOfferActionListener {
        void onAcceptClick(Offer offer);
        void onRejectClick(Offer offer);
        void onCounterClick(Offer offer);
        void onItemClick(Offer offer);
    }

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface AdapterEntryPoint {
        ItemRepository itemRepository();
    }

    public OfferAdapter(String currentUserId, OnOfferActionListener listener, Context context) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.listener = listener;

        AdapterEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.getApplicationContext(),
                AdapterEntryPoint.class
        );
        this.itemRepository = hiltEntryPoint.itemRepository();
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // << SỬA Ở ĐÂY: Đảm bảo tên Binding khớp với tên file layout của bạn >>
        // Ví dụ: nếu file là item_offer.xml -> ItemOfferBinding
        ItemOfferBinding binding = ItemOfferBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new OfferViewHolder(binding, listener, currentUserId, itemRepository);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfferBinding binding;
        private final OnOfferActionListener listener;
        private final String currentUserId;
        private final ItemRepository itemRepository;

        OfferViewHolder(ItemOfferBinding binding, OnOfferActionListener listener, String currentUserId, ItemRepository itemRepository) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.currentUserId = currentUserId;
            this.itemRepository = itemRepository;
        }

        void bind(Offer offer) {
            if (offer == null) return;

            final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            final boolean isUserTheSeller = currentUserId.equals(offer.getSellerId());

            // Lấy thông tin sản phẩm
            itemRepository.getItemById(offer.getItemId(), new Callback<Item>() {
                @Override
                public void onSuccess(Item item) {
                    if (item != null && binding != null) {
                        binding.textViewProductName.setText(item.getTitle());
                        // Hiển thị giá gốc của sản phẩm
                        binding.textViewOriginalPrice.setText(currencyFormat.format(item.getPrice()));
                        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                            Glide.with(itemView.getContext())
                                    .load(item.getImageUrls().get(0))
                                    .placeholder(R.drawable.ic_placeholder_image)
                                    .into(binding.imageViewProduct);
                        }
                    }
                }
                @Override public void onFailure(@NonNull Exception e) { /* Do nothing */ }
            });

            // Hiển thị thông tin offer
            // Nếu là người bán xem, hiển thị tên người mua. Nếu là người mua xem, ẩn đi.
            binding.textViewUserName.setText(isUserTheSeller ? offer.getBuyerDisplayName() : "");
            binding.textViewUserName.setVisibility(isUserTheSeller ? View.VISIBLE : View.GONE);

            // Hiển thị giá đang đàm phán
            binding.textViewOfferPrice.setText(currencyFormat.format(offer.getCurrentPrice()));

            // Hiển thị thời gian
            if (offer.getUpdatedAt() != null) {
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        offer.getUpdatedAt().toDate().getTime(), now, DateUtils.MINUTE_IN_MILLIS);
                binding.textViewTimestamp.setText(relativeTime);
            }

            // Gán listener cho các nút
            binding.buttonAccept.setOnClickListener(v -> listener.onAcceptClick(offer));
            binding.buttonReject.setOnClickListener(v -> listener.onRejectClick(offer));
            binding.buttonCounter.setOnClickListener(v -> listener.onCounterClick(offer));
            itemView.setOnClickListener(v -> listener.onItemClick(offer));

            // === LOGIC HIỂN THỊ ĐÚNG VỚI LAYOUT MỚI ===
            binding.layoutActionButtons.setVisibility(View.GONE);
            binding.chipStatus.setVisibility(View.GONE);

            boolean isCurrentUserTheLastActor = currentUserId.equals(offer.getLastActionByUid());

            switch (offer.getStatus().toLowerCase()) {
                case "pending":
                    if (isCurrentUserTheLastActor) {
                        // Tôi vừa trả giá, giờ tôi phải chờ
                        binding.chipStatus.setVisibility(View.VISIBLE);
                        binding.chipStatus.setText("Waiting for response");
                        // Tùy chọn: set màu cho chip
                        binding.chipStatus.setChipBackgroundColorResource(R.color.status_pending_background);
                        binding.chipStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_pending_text));
                    } else {
                        // Đến lượt tôi quyết định
                        binding.layoutActionButtons.setVisibility(View.VISIBLE);
                    }
                    break;
                case "accepted":
                    binding.chipStatus.setVisibility(View.VISIBLE);
                    binding.chipStatus.setText("Accepted");
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_completed_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_completed_text));
                    break;
                case "rejected":
                    binding.chipStatus.setVisibility(View.VISIBLE);
                    binding.chipStatus.setText("Rejected");
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_sold_background); // Dùng màu xám
                    binding.chipStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_error));
                    break;
                default: // expired, ...
                    binding.chipStatus.setVisibility(View.VISIBLE);
                    binding.chipStatus.setText(offer.getStatus());
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_default_background);
                    binding.chipStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_default_text));
                    break;
            }
        }
    }

    private static final DiffUtil.ItemCallback<Offer> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Offer>() {
                @Override
                public boolean areItemsTheSame(@NonNull Offer oldItem, @NonNull Offer newItem) {
                    // So sánh ID vẫn an toàn vì nó là @DocumentId, không bao giờ null
                    return oldItem.getOfferId().equals(newItem.getOfferId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Offer oldItem, @NonNull Offer newItem) {
                    // << SỬA LẠI ĐỂ AN TOÀN VỚI NULL >>
                    // Sử dụng java.util.Objects.equals
                    return java.util.Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                            oldItem.getCurrentPrice() == newItem.getCurrentPrice() &&
                            java.util.Objects.equals(oldItem.getLastActionByUid(), newItem.getLastActionByUid());
                }
            };
}