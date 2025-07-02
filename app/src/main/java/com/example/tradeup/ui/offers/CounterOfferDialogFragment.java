package com.example.tradeup.ui.offers;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.tradeup.R;
import com.example.tradeup.databinding.DialogCounterOfferBinding; // Sử dụng binding bạn đã tạo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CounterOfferDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "CounterOfferDialog";
    private static final String ARG_OFFER_ID = "offer_id";

    private DialogCounterOfferBinding binding;
    private OffersViewModel viewModel;
    private String offerId;

    // Factory method để tạo instance mới và truyền offerId
    public static CounterOfferDialogFragment newInstance(@NonNull String offerId) {
        CounterOfferDialogFragment fragment = new CounterOfferDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OFFER_ID, offerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            offerId = getArguments().getString(ARG_OFFER_ID);
        }
        // Lấy ViewModel từ Fragment cha (OffersFragment) để chia sẻ dữ liệu
        viewModel = new ViewModelProvider(requireParentFragment()).get(OffersViewModel.class);

        // Style cho BottomSheet
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCounterOfferBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    private void setupListeners() {
        binding.buttonSendCounterOffer.setOnClickListener(v -> {
            String priceStr = binding.editTextCounterPrice.getText().toString();
            String message = binding.editTextCounterMessage.getText().toString().trim();

            if (TextUtils.isEmpty(priceStr)) {
                binding.tilCounterPrice.setError("Price cannot be empty");
                return;
            }
            binding.tilCounterPrice.setError(null);

            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    binding.tilCounterPrice.setError("Price must be greater than 0");
                    return;
                }
                // Nếu offerId hợp lệ, gọi ViewModel
                if (offerId != null) {
                    viewModel.sendCounterOffer(offerId, price, message);
                    dismiss(); // Tự động đóng dialog sau khi gửi
                } else {
                    Toast.makeText(getContext(), "Error: Offer ID is missing.", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                binding.tilCounterPrice.setError("Invalid number format");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}