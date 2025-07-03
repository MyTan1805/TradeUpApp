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
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.databinding.DialogCounterOfferBinding;
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
        // Lấy ViewModel từ Fragment cha (OffersFragment)
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
        observeViewModel();
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
                if (offerId != null) {
                    viewModel.submitCounterOffer(offerId, price, message); // Sửa thành submitCounterOffer
                    // Không đóng dialog ngay, chờ thông báo từ ViewModel
                } else {
                    Toast.makeText(getContext(), "Error: Offer ID is missing.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            } catch (NumberFormatException e) {
                binding.tilCounterPrice.setError("Invalid number format");
            }
        });
    }

    private void observeViewModel() {
        // Quan sát thông báo Toast từ ViewModel
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                if (message.contains("Counter offer sent")) {
                    dismiss(); // Đóng dialog khi gửi thành công
                }
            }
        });

        // Quan sát trạng thái tải
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonSendCounterOffer.setEnabled(!isLoading);
            // Có thể thêm ProgressBar trong dialog nếu cần
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}