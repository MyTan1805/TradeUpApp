// File: src/main/java/com/example/tradeup/ui/offers/PaymentSelectionFragment.java
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
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.DialogPaymentSelectionBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentSelectionFragment extends BottomSheetDialogFragment {

    public static final String TAG = "PaymentSelectionDialog";
    private static final String ARG_TRANSACTION = "transaction";

    private DialogPaymentSelectionBinding binding;
    private OffersViewModel viewModel;
    private Transaction transaction;

    public static PaymentSelectionFragment newInstance(@NonNull Transaction transaction) {
        PaymentSelectionFragment fragment = new PaymentSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRANSACTION, transaction);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);
        if (getArguments() != null) {
            transaction = getArguments().getParcelable(ARG_TRANSACTION);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(OffersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPaymentSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Xử lý logic ẩn/hiện trường địa chỉ và bật/tắt nút confirm
        binding.radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            binding.buttonConfirmPayment.setEnabled(true); // Bật nút khi đã chọn
            if (checkedId == R.id.radioButtonCOD) {
                binding.tilDeliveryAddress.setVisibility(View.VISIBLE);
            } else {
                binding.tilDeliveryAddress.setVisibility(View.GONE);
            }
        });

        // Xử lý sự kiện nhấn nút Confirm
        binding.buttonConfirmPayment.setOnClickListener(v -> {
            int selectedId = binding.radioGroupPayment.getCheckedRadioButtonId();
            if (selectedId == R.id.radioButtonCOD) {
                // ... (logic COD giữ nguyên)
            } else if (selectedId == R.id.radioButtonOnline) {
                // << SỬA Ở ĐÂY >>
                // Điều hướng đến fragment thanh toán của Stripe
                Bundle args = new Bundle();
                args.putParcelable("transaction", transaction);

                NavHostFragment.findNavController(requireParentFragment())
                        .navigate(R.id.action_global_to_stripePaymentFragment, args);

                dismiss(); // Đóng dialog chọn phương thức
            }
        });
    }

    private void observeViewModel() {
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // Nếu thành công, tự động đóng dialog
                if (message.contains("selected") || message.contains("Processing")) {
                    dismiss();
                }
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonConfirmPayment.setEnabled(!isLoading);
            binding.buttonConfirmPayment.setText(isLoading ? "Processing..." : "Confirm Selection");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}