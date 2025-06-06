package com.example.tradeup.data.repository

import com.example.tradeup.data.model.User
import com.example.tradeup.data.source.remote.FirestoreUserSource
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreUserSource: FirestoreUserSource
) : UserRepository {

    override suspend fun createUserProfile(user: User): Result<Unit> {
        return firestoreUserSource.createUserProfile(user)
    }

    override suspend fun getUserProfile(uid: String): Result<User?> {
        return firestoreUserSource.getUserProfile(uid)
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return firestoreUserSource.updateUserProfile(user)
    }
}