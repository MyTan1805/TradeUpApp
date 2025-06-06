package com.example.tradeup.data.repository

import com.example.tradeup.data.source.remote.FirebaseAuthSource
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthSource: FirebaseAuthSource
) : AuthRepository {

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuthSource.getCurrentUser()
    }

    override suspend fun registerUser(email: String, pass: String): Result<FirebaseUser> {
        return firebaseAuthSource.registerUser(email, pass)
    }

    override suspend fun loginUser(email: String, pass: String): Result<FirebaseUser> {
        return firebaseAuthSource.loginUser(email, pass)
    }

    override fun logoutUser() {
        firebaseAuthSource.logoutUser()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return firebaseAuthSource.sendPasswordResetEmail(email)
    }
}