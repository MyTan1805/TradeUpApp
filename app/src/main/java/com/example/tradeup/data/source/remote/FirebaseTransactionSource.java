// File: src/main/java/com/example/tradeup/data/source/remote/FirebaseTransactionSource.java
package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Transaction;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseTransactionSource {

    private final CollectionReference transactionsCollection;

    @Inject
    public FirebaseTransactionSource(FirebaseFirestore firestore) {
        this.transactionsCollection = firestore.collection("transactions");
    }

    public Task<String> createTransaction(@NonNull Transaction transaction) {
        // Tạo một document mới với ID tự động và trả về ID đó
        return transactionsCollection.add(transaction)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().getId();
                });
    }

    public Task<Transaction> getTransactionById(String transactionId) {
        return transactionsCollection.document(transactionId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    // Firestore sẽ tự gán ID vào trường có @DocumentId
                    return task.getResult().toObject(Transaction.class);
                });
    }

    public Task<List<Transaction>> getTransactionsByUser(String userId, @NonNull String asRole, long limit) {
        return transactionsCollection
                .whereEqualTo(asRole, userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toObjects(Transaction.class);
                });
    }

    public Task<Transaction> getTransactionByItemId(String itemId) {
        return transactionsCollection
                .whereEqualTo("itemId", itemId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        return querySnapshot.getDocuments().get(0).toObject(Transaction.class);
                    }
                    return null;
                });
    }

    public Task<Void> updateTransaction(String transactionId, Map<String, Object> updates) {
        return transactionsCollection.document(transactionId).update(updates);
    }
}