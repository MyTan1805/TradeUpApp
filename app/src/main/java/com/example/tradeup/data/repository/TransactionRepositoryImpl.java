package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Transaction;
import com.google.firebase.firestore.*;
import java.util.*;
import javax.inject.Inject; // <<< THÊM IMPORT NÀY
import javax.inject.Singleton;

@Singleton
public class TransactionRepositoryImpl implements TransactionRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TRANSACTIONS_COLLECTION = "transactions";

    @Inject
    public TransactionRepositoryImpl() {
    }

    @Override
    public void createTransaction(@NonNull Transaction transaction, Callback<String> callback) {
        db.collection(TRANSACTIONS_COLLECTION)
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    String transactionId = documentReference.getId();
                    // Cập nhật transactionId vào document
                    documentReference.update("transactionId", transactionId)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(transactionId))
                            .addOnFailureListener(e -> callback.onFailure(e));
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getTransactionById(String transactionId, Callback<Transaction> callback) {
        if (transactionId == null || transactionId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Transaction ID cannot be null or empty"));
            return;
        }

        db.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            Transaction transaction = doc.toObject(Transaction.class);
                            if (transaction != null) {
                                transaction.setTransactionId(doc.getId());
                            }
                            callback.onSuccess(transaction);
                        } else {
                            callback.onSuccess(null); // Không tìm thấy transaction
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    @Override
    public void getTransactionsByUser(String userId, @NonNull String asRole, long limit, Callback<List<Transaction>> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID cannot be null or empty"));
            return;
        }

        if (!"buyerId".equals(asRole) && !"sellerId".equals(asRole)) {
            callback.onFailure(new IllegalArgumentException("asRole must be either 'buyerId' or 'sellerId'"));
            return;
        }

        db.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo(asRole, userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> transactions = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Transaction transaction = doc.toObject(Transaction.class);
                            transaction.setTransactionId(doc.getId());
                            transactions.add(transaction);
                        }
                        callback.onSuccess(transactions);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    // Thêm các phương thức bổ sung nếu cần
    public void updateTransactionStatus(String transactionId, String newStatus, Callback<Void> callback) {
        db.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .update("paymentStatus", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateTransaction(String transactionId, Map<String, Object> updates, Callback<Void> callback) {
        db.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}