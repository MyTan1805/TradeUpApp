// File: src/main/java/com/example/tradeup/ui/offers/MakeOfferDialogFragment.java

package com.example.tradeup.ui.offers;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider; // Thêm import này

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.DialogMakeOfferBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint; // Thêm import Hilt

@AndroidEntryPoint // Đánh dấu để Hilt có thể inject ViewModel
public class MakeOfferDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_ITEM = "arg_item";

    private DialogMakeOfferBinding binding;
    private Item currentItem;

    // Khai báo ViewModel
    private OffersViewModel viewModel;

    public static MakeOfferDialogFragment newInstance(Item item) {
        MakeOfferDialogFragment fragment = new MakeOfferDialogFragment();
        Bundle args = new Bundle();
        // Quan trọng: Item phải implements Parcelable để truyền qua Bundle
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);

        if (getArguments() != null) {
            currentItem = getArguments().getParcelable(ARG_ITEM);
        }

        // Khởi tạo ViewModel, scope là của chính Dialog này
        // hoặc của Fragment cha nếu muốn chia sẻ trạng thái
        viewModel = new ViewModelProvider(this).get(OffersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogMakeOfferBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentItem == null) {
            Toast.makeText(getContext(), "Error: Item data is missing.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        setupUI();
        setupClickListeners();
        observeViewModel();
    }

    private void setupUI() {
        binding.textViewProductName.setText(currentItem.getTitle());
        String originalPrice = NumberFormat.getCurrencyInstance(Locale.US).format(currentItem.getPrice());
        binding.textViewOriginalPrice.setText("Original price: " + originalPrice);

        if (currentItem.getImageUrls() != null && !currentItem.getImageUrls().isEmpty()) {
            Glide.with(this)
                    .load(currentItem.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(binding.imageViewProduct);
        }
    }

    private void setupClickListeners() {
        binding.buttonClose.setOnClickListener(v -> dismiss());
        binding.buttonCancelOffer.setOnClickListener(v -> dismiss());

        binding.buttonSendOffer.setOnClickListener(v -> {
            if (validateInput()) {
                double offerAmount = Double.parseDouble(binding.editTextOfferAmount.getText().toString());
                String message = binding.editTextMessage.getText().toString().trim();

                // Gọi ViewModel để tạo offer
                    viewModel.createOffer(currentItem, offerAmount, message);
            }
        });
    }

    private void observeViewModel() {
        // Lắng nghe thông báo từ ViewModel
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // Nếu gửi thành công, đóng dialog
                if (message.contains("successfully")) {
                    dismiss();
                }
            }
        });

        // Lắng nghe trạng thái loading để vô hiệu hóa nút
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonSendOffer.setEnabled(!isLoading);
            binding.buttonSendOffer.setText(isLoading ? "Sending..." : "Send Offer");
        });
    }

    private boolean validateInput() {
        String amountText = binding.editTextOfferAmount.getText().toString();
        if (TextUtils.isEmpty(amountText)) {
            binding.tilOfferAmount.setError("Please enter an offer amount.");
            return false;
        }
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                binding.tilOfferAmount.setError("Offer must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.tilOfferAmount.setError("Invalid number format.");
            return false;
        }

        binding.tilOfferAmount.setError(null);
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}