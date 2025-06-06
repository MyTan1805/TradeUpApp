package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Transaction
import com.example.tradeup.data.source.remote.FirebaseTransactionSource // Giả sử bạn sẽ tạo FirebaseTransactionSource.kt
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val firebaseTransactionSource: FirebaseTransactionSource
) : TransactionRepository {

    override suspend fun createTransaction(transaction: Transaction): Result<String> {
        // return firebaseTransactionSource.createTransaction(transaction)
        TODO("Implement createTransaction in FirebaseTransactionSource and call it here")
    }

    override suspend fun getTransactionById(transactionId: String): Result<Transaction?> {
        // return firebaseTransactionSource.getTransactionById(transactionId)
        TODO("Implement getTransactionById in FirebaseTransactionSource and call it here")
    }

    override suspend fun getTransactionsByUser(userId: String, asRole: String): Result<List<Transaction>> {
        // return firebaseTransactionSource.getTransactionsByUser(userId, asRole)
        TODO("Implement getTransactionsByUser in FirebaseTransactionSource and call it here")
    }
    // ...
}