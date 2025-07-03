// File: src/main/java/com/example/tradeup/data/repository/TransactionRepositoryImpl.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.data.source.remote.FirebaseTransactionSource;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionRepositoryImpl implements TransactionRepository {

    private final FirebaseTransactionSource firebaseTransactionSource;

    @Inject
    public TransactionRepositoryImpl(FirebaseTransactionSource firebaseTransactionSource) {
        this.firebaseTransactionSource = firebaseTransactionSource;
    }

    @Override
    public void createTransaction(@NonNull Transaction transaction, Callback<String> callback) {
        firebaseTransactionSource.createTransaction(transaction)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getTransactionById(String transactionId, Callback<Transaction> callback) {
        firebaseTransactionSource.getTransactionById(transactionId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getTransactionsByUser(String userId, @NonNull String asRole, long limit, Callback<List<Transaction>> callback) {
        firebaseTransactionSource.getTransactionsByUser(userId, asRole, limit)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getTransactionByItemId(String itemId, Callback<Transaction> callback) {
        firebaseTransactionSource.getTransactionByItemId(itemId)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateTransaction(String transactionId, Map<String, Object> updates, Callback<Void> callback) {
        firebaseTransactionSource.updateTransaction(transactionId, updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}