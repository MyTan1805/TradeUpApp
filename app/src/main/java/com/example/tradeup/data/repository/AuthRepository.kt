package com.example.tradeup.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun registerUser(email: String, pass: String): Result<FirebaseUser>
    suspend fun loginUser(email: String, pass: String): Result<FirebaseUser>
    fun logoutUser()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(user: FirebaseUser): Result<Unit>
    // Thêm các hàm khác nếu cần, ví dụ: sendEmailVerification
}