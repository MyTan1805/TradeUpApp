package com.example.tradeup.ui.profile;

import android.util.Log;
import androidx.annotation.NonNull;
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
    private void fetchTransactions(String role) {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _toastMessage.setValue(new Event<>("Please log in to view transactions."));
            _isLoading.setValue(false);
            return;
        }

        String userId = currentUser.getUid();

        // TODO: Logic get all transactions chưa có trong repository.
        // Hiện tại, chúng ta sẽ tạm thời tải cả 2 danh sách mua và bán khi role là null.
        // Đây là cách làm tạm thời và cần được tối ưu sau.
        if (role == null) {
            // Tạm thời chỉ tải giao dịch mua
            fetchTransactionsByRole(userId, "buyerId");
        } else {
            fetchTransactionsByRole(userId, role);
        }
    }

    private void fetchTransactionsByRole(String userId, String role) {
        transactionRepository.getTransactionsByUser(userId, role, TRANSACTION_LIMIT, new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
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