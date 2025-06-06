package com.example.tradeup.data.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthSource @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun registerUser(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            // Sau khi .await() thành công, authResult.user không thể null
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            // Sau khi .await() thành công, authResult.user không thể null
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser() {
        firebaseAuth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(user: FirebaseUser): Result<Unit> {
        return try {
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bạn có thể thêm các hàm khác liên quan đến Firebase Authentication nếu cần
    // Ví dụ: cập nhật email, cập nhật mật khẩu, liên kết với Google Sign-In, v.v.
}