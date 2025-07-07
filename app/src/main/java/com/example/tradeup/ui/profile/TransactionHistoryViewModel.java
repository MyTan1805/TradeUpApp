package com.example.tradeup.ui.profile;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.TransactionRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

@HiltViewModel
public class TransactionHistoryViewModel extends ViewModel {

    private static final String TAG = "TransactionHistoryVM";
    private static final long TRANSACTION_LIMIT = 20;

    private final TransactionRepository transactionRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getTransactions() { return _transactions; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    // Biến để lưu trạng thái filter hiện tại
    private String currentFilterRole = null; // null để tải tất cả, "buyerId" hoặc "sellerId"

    @Inject
    public TransactionHistoryViewModel(TransactionRepository transactionRepository, AuthRepository authRepository) {
        this.transactionRepository = transactionRepository;
        this.authRepository = authRepository;
        loadInitialTransactions(); // Tải dữ liệu lần đầu
    }

    /**
     * Tải danh sách giao dịch ban đầu (tải tất cả).
     */
    private void loadInitialTransactions() {
        onAllFilterClicked();
    }

    /**
     * Hàm trung tâm để tải dữ liệu từ repository.
     * @param role Tên trường để query ("buyerId" hoặc "sellerId"). Nếu null, sẽ tải tất cả.
     */
    private void fetchTransactions(@Nullable String role) { // << Thêm @Nullable
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _toastMessage.setValue(new Event<>("Please log in to view transactions."));
            _isLoading.setValue(false);
            return;
        }

        String userId = currentUser.getUid();

        // << SỬA LẠI HOÀN TOÀN LOGIC NÀY >>
        if (role == null) {
            // Trường hợp tải "All": Tải cả danh sách mua và bán
            fetchAllUserTransactions(userId);
        } else {
            // Trường hợp tải "Purchases" hoặc "Sales"
            fetchTransactionsByRole(userId, role);
        }
    }

    private void fetchTransactionsByRole(String userId, String role) {
        transactionRepository.getTransactionsByUser(userId, role, TRANSACTION_LIMIT, new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                if (data != null) {
                    // Sắp xếp theo ngày mới nhất
                    data.sort(Comparator.comparing(Transaction::getTransactionDate, Comparator.nullsLast(Comparator.reverseOrder())));
                }
                _transactions.setValue(data != null ? data : Collections.emptyList());
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to fetch transactions for role " + role, e);
                _toastMessage.setValue(new Event<>("Error loading transactions."));
                _isLoading.setValue(false);
            }
        });
    }

    private void fetchAllUserTransactions(String userId) {
        final List<Transaction> allTransactions = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger taskCounter = new AtomicInteger(2); // Chúng ta có 2 tác vụ cần hoàn thành

        Callback<List<Transaction>> commonCallback = new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                if (data != null) {
                    allTransactions.addAll(data);
                }
                finishTask();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "A part of fetching all transactions failed", e);
                // Vẫn giảm counter để không bị treo loading mãi mãi
                finishTask();
            }

            private void finishTask() {
                if (taskCounter.decrementAndGet() == 0) {
                    // Khi cả 2 tác vụ đã xong, sắp xếp và cập nhật UI
                    allTransactions.sort(Comparator.comparing(Transaction::getTransactionDate, Comparator.nullsLast(Comparator.reverseOrder())));
                    _transactions.postValue(allTransactions);
                    _isLoading.postValue(false);
                }
            }
        };

        // Bắt đầu 2 tác vụ song song
        transactionRepository.getTransactionsByUser(userId, "buyerId", TRANSACTION_LIMIT, commonCallback);
        transactionRepository.getTransactionsByUser(userId, "sellerId", TRANSACTION_LIMIT, commonCallback);
    }

    // --- Xử lý sự kiện từ các Chip Filter ---

    public void onAllFilterClicked() {
        currentFilterRole = null;
        fetchTransactions("buyerId");
    }

    public void onPurchasesFilterClicked() {
        currentFilterRole = "buyerId";
        fetchTransactions(currentFilterRole);
    }

    public void onSalesFilterClicked() {
        currentFilterRole = "sellerId";
        fetchTransactions(currentFilterRole);
    }
}