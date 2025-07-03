package com.example.tradeup.ui.offers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tradeup.databinding.FragmentPaymentSelectionBinding;
import com.example.tradeup.data.model.Transaction;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PaymentSelectionFragment extends BottomSheetDialogFragment {

    public static final String TAG = "PaymentSelectionDialog";
    private static final String ARG_TRANSACTION = "transaction";

    private FragmentPaymentSelectionBinding binding;
    private OffersViewModel viewModel;
    private Transaction transaction;

    public static PaymentSelectionFragment newInstance(@NonNull Transaction transaction) {
        PaymentSelectionFragment fragment = new PaymentSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRANSACTION, transaction); // Transaction is now Parcelable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transaction = getArguments().getParcelable(ARG_TRANSACTION);
        }
        viewModel = new ViewModelProvider(requireParentFragment()).get(OffersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaymentSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.buttonSelectCOD.setOnClickListener(v -> {
            String deliveryAddress = "Nhập địa chỉ ở đây"; // Lấy từ EditText hoặc SharedPreferences
            viewModel.selectPaymentMethod(transaction.getTransactionId(), "COD", deliveryAddress);
            dismiss();
        });

        binding.buttonSelectOnline.setOnClickListener(v -> {
            viewModel.selectPaymentMethod(transaction.getTransactionId(), "Online", null);
            dismiss();
        });
    }

    private void observeViewModel() {
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonSelectCOD.setEnabled(!isLoading);
            binding.buttonSelectOnline.setEnabled(!isLoading);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}