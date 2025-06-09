package com.example.tradeup.data.repository

import com.example.tradeup.data.model.User

interface UserRepository {
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<User?>
    suspend fun updateUserProfile(user: User): Result<Unit>

}