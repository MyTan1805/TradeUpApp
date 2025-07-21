// File: src/main/java/com/example/tradeup/ui/profile/TransactionHistoryFragment.java
package com.example.tradeup.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentTransactionHistoryBinding;
import com.example.tradeup.ui.adapters.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.tradeup.ui.offers.PaymentSelectionFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionHistoryFragment extends Fragment implements TransactionAdapter.OnTransactionActionListener {

    private static final String TAG = "TransactionHistoryFrag";

    private FragmentTransactionHistoryBinding binding;
    private TransactionHistoryViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionHistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        // Các hàm setup giữ nguyên
        setupToolbar();
        setupRecyclerView();
        setupChipListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
    }

    private void setupRecyclerView() {
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.e(TAG, "FATAL: Current user is null. Cannot setup adapter correctly.");
            Toast.makeText(getContext(), "User session expired. Please log in again.", Toast.LENGTH_LONG).show();
            navController.navigateUp();
            return;
        }

        transactionAdapter = new TransactionAdapter(currentUserId, this);
        binding.recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void setupChipListeners() {
        binding.chipGroupFilters.check(R.id.chipAll);
        binding.chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                viewModel.onAllFilterClicked();
            } else if (checkedId == R.id.chipPurchases) {
                viewModel.onPurchasesFilterClicked();
            } else if (checkedId == R.id.chipSales) {
                viewModel.onSalesFilterClicked();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean isListCurrentlyEmpty = transactionAdapter.getCurrentList().isEmpty();
            if (isLoading && isListCurrentlyEmpty) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerViewTransactions.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactionViewDataList -> {
            boolean isLoading = viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue();

            transactionAdapter.submitList(transactionViewDataList);

            if (!isLoading) {
                boolean isEmpty = transactionViewDataList == null || transactionViewDataList.isEmpty();
                binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.recyclerViewTransactions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Callbacks từ Adapter (Giữ nguyên, đã rất tốt) ---
    @Override
    public void onTransactionClick(Transaction transaction) {
        if (isAdded() && transaction.getItemId() != null) {
            Bundle args = new Bundle();
            args.putString("itemId", transaction.getItemId());
            navController.navigate(R.id.action_global_to_itemDetailFragment, args);
        }
    }

    @Override
    public void onRateClick(Transaction transaction) {
        if (!isAdded()) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You need to be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();
        boolean isUserTheBuyer = transaction.getBuyerId().equals(currentUserId);
        String ratedUserId = isUserTheBuyer ? transaction.getSellerId() : transaction.getBuyerId();

        Bundle args = new Bundle();
        args.putString("transactionId", transaction.getTransactionId());
        args.putString("itemId", transaction.getItemId());
        args.putString("ratedUserId", ratedUserId);

        try {
            navController.navigate(R.id.action_global_to_submitReviewFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Navigation to SubmitReviewFragment failed", e);
            Toast.makeText(getContext(), "Could not open review screen.", Toast.LENGTH_SHORT).show();
        }
    }

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
        if (isAdded() && transaction != null) {
            PaymentSelectionFragment.newInstance(transaction)
                    .show(getParentFragmentManager(), PaymentSelectionFragment.TAG);
        }
    }
}