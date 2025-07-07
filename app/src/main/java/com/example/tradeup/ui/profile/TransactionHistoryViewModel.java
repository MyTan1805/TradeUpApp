// File: src/main/java/com/example/tradeup/ui/profile/TransactionHistoryViewModel.java
// << PHIÊN BẢN ĐÃ NÂNG CẤP >>

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
import com.google.firebase.firestore.FieldValue;

import com.example.tradeup.data.model.Notification;
import com.example.tradeup.data.repository.NotificationRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.tradeup.data.repository.payment.StripeRepository;
import com.stripe.android.paymentsheet.PaymentSheet;

@HiltViewModel
public class TransactionHistoryViewModel extends ViewModel {

    private static final String TAG = "TransactionHistoryVM";
    private static final long TRANSACTION_LIMIT = 50; // Tăng limit lên một chút

    private final NotificationRepository notificationRepository;

    private final TransactionRepository transactionRepository;
    private final AuthRepository authRepository;

    private final StripeRepository stripeRepository; // << THÊM BIẾN NÀY

    // LiveData để gửi client_secret và ephemeralKey về cho Fragment
    private final MutableLiveData<Event<Map<String, String>>> _paymentSheetParams = new MutableLiveData<>();
    public LiveData<Event<Map<String, String>>> getPaymentSheetParams() { return _paymentSheetParams; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getTransactions() { return _transactions; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    @Inject
    public TransactionHistoryViewModel(TransactionRepository transactionRepository, AuthRepository authRepository,
                                       NotificationRepository notificationRepository,
                                       StripeRepository stripeRepository) {
        this.transactionRepository = transactionRepository;
        this.authRepository = authRepository;
        this.notificationRepository = notificationRepository;
        this.stripeRepository = stripeRepository;
        onAllFilterClicked(); // Tải tất cả giao dịch khi ViewModel được tạo
    }

    private void sendTransactionNotification(Transaction transaction, String type, String receiverId) {
        Notification notif = new Notification();
        notif.setUserId(receiverId);
        notif.setType(type); // "transaction_update", "item_shipped", etc.
        notif.setRelatedContentId(transaction.getItemId());
        notif.setImageUrl(transaction.getItemImageUrl());

        // Tạo title và message dựa trên type
        switch (type) {
            case "payment_method_selected":
                notif.setTitle("Payment Method Selected");
                notif.setMessage(String.format("The buyer has selected %s for your item '%s'. Please prepare for shipment.",
                        transaction.getPaymentMethod(), transaction.getItemTitle()));
                break;
            case "item_shipped":
                notif.setTitle("Your Item is on its Way!");
                notif.setMessage(String.format("The seller has shipped your item '%s'.", transaction.getItemTitle()));
                break;
            case "transaction_completed":
                notif.setTitle("Transaction Completed!");
                notif.setMessage(String.format("Your transaction for '%s' is complete. You can now rate the other party.", transaction.getItemTitle()));
                break;
        }

        notificationRepository.createNotification(notif, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Successfully sent notification of type: " + type);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to send notification of type: " + type, e);
            }
        });
    }

    public void completeOnlinePayment(String transactionId) {
        _isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "completed");
        // Đối với thanh toán online, hàng vẫn cần được giao
        updates.put("shippingStatus", "waiting_for_shipment");
        updates.put("transactionDate", FieldValue.serverTimestamp());

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Online payment successful!");
                // Gửi thông báo cho người bán rằng người mua đã thanh toán
                transactionRepository.getTransactionById(transactionId, new Callback<Transaction>() {
                    @Override public void onSuccess(Transaction t) {
                        if (t != null) {
                            sendTransactionNotification(t, "online_payment_success", t.getSellerId());
                        }
                    }
                    @Override public void onFailure(@NonNull Exception e) {}
                });
                // Tải lại danh sách giao dịch của người mua
                fetchTransactionsByRole("buyerId");
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to update transaction: " + e.getMessage());
            }
        });
    }

    public void startOnlinePaymentFlow(Transaction transaction) {
        _isLoading.setValue(true);
        String currency = "usd";

        // Lấy tên người mua từ Transaction hoặc User nếu có
        // Tạm thời dùng tên mặc định
        String customerName = "A TradeUp User";

        stripeRepository.createPaymentIntent(transaction.getPriceSold(), currency, customerName, new Callback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> result) {
                _paymentSheetParams.postValue(new Event<>(result));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to initialize payment: " + e.getMessage());
            }
        });
    }

    public void failOnlinePayment(String transactionId) {
        _isLoading.postValue(false);
        showError("Payment failed or was cancelled.");
        // Có thể cập nhật trạng thái giao dịch thành "failed" nếu cần
    }

    private void fetchTransactionsByRole(@Nullable String role) {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _toastMessage.setValue(new Event<>("Please log in to view transactions."));
            _isLoading.setValue(false);
            return;
        }

        String userId = currentUser.getUid();

        if (role == null) { // Tải tất cả
            fetchAllUserTransactions(userId);
        } else { // Tải theo vai trò cụ thể
            transactionRepository.getTransactionsByUser(userId, role, TRANSACTION_LIMIT, new Callback<List<Transaction>>() {
                @Override
                public void onSuccess(List<Transaction> data) {
                    _transactions.postValue(data != null ? data : Collections.emptyList());
                    _isLoading.postValue(false);
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    showError("Error loading transactions: " + e.getMessage());
                }
            });
        }
    }

    private void fetchAllUserTransactions(String userId) {
        final List<Transaction> allTransactions = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger taskCounter = new AtomicInteger(2);

        Callback<List<Transaction>> commonCallback = new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                if (data != null) allTransactions.addAll(data);
                finishTask();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "A part of fetching all transactions failed", e);
                finishTask();
            }

            private void finishTask() {
                if (taskCounter.decrementAndGet() == 0) {
                    allTransactions.sort(Comparator.comparing(Transaction::getTransactionDate, Comparator.nullsLast(Comparator.reverseOrder())));
                    _transactions.postValue(allTransactions);
                    _isLoading.postValue(false);
                }
            }
        };

        transactionRepository.getTransactionsByUser(userId, "buyerId", TRANSACTION_LIMIT, commonCallback);
        transactionRepository.getTransactionsByUser(userId, "sellerId", TRANSACTION_LIMIT, commonCallback);
    }

    // --- Xử lý sự kiện từ Filter Chips ---
    public void onAllFilterClicked() { fetchTransactionsByRole(null); }
    public void onPurchasesFilterClicked() { fetchTransactionsByRole("buyerId"); }
    public void onSalesFilterClicked() { fetchTransactionsByRole("sellerId"); }

    // === CÁC HÀM LOGIC ĐƯỢC THÊM VÀO ===

    public void markAsShipped(String transactionId) {
        _isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("shippingStatus", "shipped");
        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Marked as shipped.");
                // << GỌI HÀM THÔNG BÁO >>
                transactionRepository.getTransactionById(transactionId, new Callback<Transaction>() {
                    @Override public void onSuccess(Transaction updatedTransaction) {
                        if (updatedTransaction != null) {
                            // Gửi thông báo cho người mua
                            sendTransactionNotification(updatedTransaction, "item_shipped", updatedTransaction.getBuyerId());
                        }
                    }
                    @Override public void onFailure(@NonNull Exception e) {}
                });

                fetchTransactionsByRole("sellerId");
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to update status: " + e.getMessage());
            }
        });
    }

    public void confirmReceipt(String transactionId) {
        _isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("shippingStatus", "completed");
        updates.put("paymentStatus", "completed");
        updates.put("transactionDate", FieldValue.serverTimestamp());

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Transaction completed!");
                // << GỌI HÀM THÔNG BÁO >>
                transactionRepository.getTransactionById(transactionId, new Callback<Transaction>() {
                    @Override public void onSuccess(Transaction updatedTransaction) {
                        if (updatedTransaction != null) {
                            // Gửi thông báo cho cả hai
                            sendTransactionNotification(updatedTransaction, "transaction_completed", updatedTransaction.getBuyerId());
                            sendTransactionNotification(updatedTransaction, "transaction_completed", updatedTransaction.getSellerId());
                        }
                    }
                    @Override public void onFailure(@NonNull Exception e) {}
                });

                fetchTransactionsByRole("buyerId");
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to complete transaction: " + e.getMessage());
            }
        });
    }

    // Hàm này được gọi từ PaymentSelectionFragment
    public void selectPaymentMethod(String transactionId, String paymentMethod, @Nullable String deliveryAddress) {
        _isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentMethod", paymentMethod);

        if ("COD".equalsIgnoreCase(paymentMethod)) {
            if (deliveryAddress != null) updates.put("deliveryAddress", deliveryAddress);
            updates.put("shippingStatus", "waiting_for_shipment");
        } else if ("Online".equalsIgnoreCase(paymentMethod)) {
            // Logic cho thanh toán online, hiện tại chỉ cập nhật trạng thái
            updates.put("shippingStatus", "waiting_for_shipment"); // Vẫn cần người bán giao hàng
        }

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Payment method selected.");
                // << GỌI HÀM THÔNG BÁO >>
                // Cần lấy lại transaction để có đủ thông tin
                transactionRepository.getTransactionById(transactionId, new Callback<Transaction>() {
                    @Override public void onSuccess(Transaction updatedTransaction) {
                        if (updatedTransaction != null) {
                            // Gửi thông báo cho người bán
                            sendTransactionNotification(updatedTransaction, "payment_method_selected", updatedTransaction.getSellerId());
                        }
                    }
                    @Override public void onFailure(@NonNull Exception e) {}
                });

                fetchTransactionsByRole("buyerId");
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to select payment method: " + e.getMessage());
            }
        });
    }

    // Các hàm tiện ích
    private void showError(String message) {
        _toastMessage.postValue(new Event<>(message));
        _isLoading.postValue(false);
    }

    private void showSuccess(String message) {
        _toastMessage.postValue(new Event<>(message));
        _isLoading.postValue(false);
    }
}