package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Transaction // Giả sử bạn có model Transaction.kt

interface TransactionRepository {
    suspend fun createTransaction(transaction: Transaction): Result<String> // Trả về transactionId
    suspend fun getTransactionById(transactionId: String): Result<Transaction?>
    suspend fun getTransactionsByUser(userId: String, asRole: String): Result<List<Transaction>> // asRole = "buyer" or "seller"
}