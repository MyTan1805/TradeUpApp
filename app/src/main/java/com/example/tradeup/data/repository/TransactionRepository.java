package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Transaction; // Model Transaction (Java)
import java.util.List;

public interface TransactionRepository {
    void createTransaction(@NonNull Transaction transaction, Callback<String> callback); // Trả về transactionId

    void getTransactionById(String transactionId, Callback<Transaction> callback); // Transaction có thể null

    // asRole sẽ là tên field trong Firestore ("buyerId" hoặc "sellerId")
    void getTransactionsByUser(String userId, @NonNull String asRole, long limit, Callback<List<Transaction>> callback);
}