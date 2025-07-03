// File: src/main/java/com/example/tradeup/data/repository/TransactionRepository.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Transaction;
import java.util.List;
import java.util.Map;

public interface TransactionRepository {
    void createTransaction(@NonNull Transaction transaction, Callback<String> callback);
    void getTransactionById(String transactionId, Callback<Transaction> callback);
    void getTransactionsByUser(String userId, @NonNull String asRole, long limit, Callback<List<Transaction>> callback);
    void getTransactionByItemId(String itemId, Callback<Transaction> callback);
    void updateTransaction(String transactionId, Map<String, Object> updates, Callback<Void> callback);
}