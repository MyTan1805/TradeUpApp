package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Transaction; // Model Transaction (Java)
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

public class FirebaseTransactionSource {

    private final CollectionReference transactionsCollection;

    @Inject
    public FirebaseTransactionSource(FirebaseFirestore firestore) {
        this.transactionsCollection = firestore.collection("transactions");
    }

    public Task<String> createTransaction(@NonNull Transaction transaction) {
        // Firestore sẽ tự xử lý @ServerTimestamp trong POJO Transaction khi thêm mới
        return transactionsCollection.add(transaction)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).getId();
                });
    }

    public Task<Transaction> getTransactionById(String transactionId) {
        return transactionsCollection.document(transactionId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    DocumentSnapshot documentSnapshot = task.getResult();
                    return documentSnapshot.toObject(Transaction.class); // Sẽ là null nếu không tồn tại
                });
    }

    public Task<List<Transaction>> getTransactionsByUser(String userId, @NonNull String asRole, long limit) {
        // asRole phải là "buyerId" hoặc "sellerId" (tên field trong Transaction model/Firestore)
        if (!"buyerId".equals(asRole) && !"sellerId".equals(asRole)) {
            return com.google.android.gms.tasks.Tasks.forException(
                    new IllegalArgumentException("Invalid role specified. Must be 'buyerId' or 'sellerId'.")
            );
        }

        return transactionsCollection
                .whereEqualTo(asRole, userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).toObjects(Transaction.class);
                });
    }
}