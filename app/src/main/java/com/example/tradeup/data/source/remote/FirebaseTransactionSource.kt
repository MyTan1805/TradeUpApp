package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.tradeup.data.model.Transaction // Đảm bảo bạn có Transaction.kt
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTransactionSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val transactionsCollection = firestore.collection("transactions")

    suspend fun createTransaction(transaction: Transaction): Result<String> { // Trả về transactionId
        return try {
            val documentReference = transactionsCollection.add(transaction).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionById(transactionId: String): Result<Transaction?> {
        return try {
            val documentSnapshot = transactionsCollection.document(transactionId).get().await()
            val transaction = documentSnapshot.toObject(Transaction::class.java)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionsByUser(userId: String, asRole: String, limit: Long = 10): Result<List<Transaction>> {
        // asRole có thể là "buyerId" hoặc "sellerId" (tên field trong Transaction model)
        if (asRole !in listOf("buyerId", "sellerId")) {
            return Result.failure(IllegalArgumentException("Invalid role specified. Must be 'buyerId' or 'sellerId'."))
        }
        return try {
            val querySnapshot = transactionsCollection
                .whereEqualTo(asRole, userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val transactions = querySnapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}