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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentTabbedListBinding;
import com.example.tradeup.ui.adapters.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private FragmentTabbedListBinding binding;
    private OffersViewModel viewModel;
    private TransactionAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(OffersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTabbedListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();
        viewModel.loadTransactions(true); // Load transactions
    }

    private void setupRecyclerView() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        adapter = new TransactionAdapter(currentUserId, new TransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Toast.makeText(getContext(), "Clicked on transaction: " + transaction.getTransactionId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRateClick(Transaction transaction) {
                Toast.makeText(getContext(), "Rate clicked: " + transaction.getTransactionId(), Toast.LENGTH_SHORT).show();
                // TODO: Điều hướng đến màn hình Rate
            }

            // << TRIỂN KHAI CÁC HÀM MỚI >>
            @Override
            public void onMarkAsShippedClick(Transaction transaction) {
                viewModel.markAsShipped(transaction.getTransactionId());
            }

            @Override
            public void onConfirmReceiptClick(Transaction transaction) {
                viewModel.confirmReceipt(transaction.getTransactionId());
            }

            @Override
            public void onProceedToPaymentClick(Transaction transaction) {
                if (isAdded()) {
                    PaymentSelectionFragment.newInstance(transaction)
                            .show(getParentFragmentManager(), PaymentSelectionFragment.TAG);
                }
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactionViewDataList -> {
            adapter.submitList(transactionViewDataList != null ? transactionViewDataList : Collections.emptyList());
            binding.textViewEmpty.setVisibility(transactionViewDataList == null || transactionViewDataList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textViewEmpty.setText("You have no transactions.");
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}