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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("ID_DEBUG", "Current User ID in Fragment: " + user.getUid());
        } else {
            Log.d("ID_DEBUG", "Current User in Fragment is NULL.");
        }

        setupToolbar();
        setupRecyclerView();
        setupChipListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
    }

    private void setupRecyclerView() {
        // Lấy userId một cách an toàn
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.e(TAG, "FATAL: Current user is null. Cannot setup adapter correctly.");
            // Có thể hiển thị một lỗi và quay lại
            Toast.makeText(getContext(), "User session expired. Please log in again.", Toast.LENGTH_LONG).show();
            navController.navigateUp();
            return;
        }

        transactionAdapter = new TransactionAdapter(currentUserId, this);
        binding.recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void setupChipListeners() {
        // Mặc định chọn chip "All" khi vào màn hình
        binding.chipGroupFilters.check(R.id.chipAll);

        binding.chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                viewModel.onAllFilterClicked();
            } else if (checkedId == R.id.chipPurchases) {
                viewModel.onPurchasesFilterClicked();
            }
            else if (checkedId == R.id.chipSales) {
                viewModel.onSalesFilterClicked();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Hiển thị ProgressBar toàn màn hình khi đang tải và danh sách rỗng
            if (isLoading && transactionAdapter.getItemCount() == 0) {
                //binding.progressBar.setVisibility(View.VISIBLE); // Giả sử có ProgressBar
            } else {
                //binding.progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            Log.d(TAG, "Observer received " + (transactions != null ? transactions.size() : "null") + " transactions.");
            transactionAdapter.submitList(transactions);

            // Hiển thị trạng thái empty view nếu danh sách rỗng
            boolean isEmpty = transactions == null || transactions.isEmpty();
            //binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE); // Giả sử có emptyView
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
        binding = null; // Tránh memory leak
    }

    // --- Callbacks từ Adapter ---

    @Override
    public void onTransactionClick(Transaction transaction) {
        // Khi click vào 1 giao dịch, điều hướng đến trang chi tiết sản phẩm của giao dịch đó
        if (isAdded() && transaction.getItemId() != null) {
            Bundle args = new Bundle();
            args.putString("itemId", transaction.getItemId());
            navController.navigate(R.id.action_global_to_itemDetailFragment, args);
        }
    }

    @Override
    public void onRateClick(Transaction transaction) {
        // Điều hướng đến màn hình đánh giá
        if (isAdded() && FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean isUserTheBuyer = transaction.getBuyerId().equals(currentUserId);

            // Người được đánh giá là đối tác trong giao dịch
            String ratedUserId = isUserTheBuyer ? transaction.getSellerId() : transaction.getBuyerId();

            Bundle args = new Bundle();
            args.putString("transactionId", transaction.getTransactionId());
            args.putString("itemId", transaction.getItemId());
            args.putString("ratedUserId", ratedUserId);

            navController.navigate(R.id.action_global_to_submitReviewFragment, args);
        }
    }

    @Override
    public void onConfirmCOD(Transaction transaction) {
        Log.d(TAG, "onConfirmCOD clicked, but it is not handled in this screen.");
        Toast.makeText(getContext(), "This action is handled in the Offers screen.", Toast.LENGTH_SHORT).show();

        // Hoặc nếu bạn muốn ViewModel này cũng xử lý, bạn phải thêm logic
        // vào TransactionHistoryViewModel. Nhưng hiện tại thì không cần thiết.
        // viewModel.confirmCODPayment(transaction.getTransactionId(), ...);
    }


}