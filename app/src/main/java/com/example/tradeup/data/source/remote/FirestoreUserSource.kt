package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.example.tradeup.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val documentSnapshot = usersCollection.document(uid).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            if (user.uid.isBlank()) {
                return Result.failure(IllegalArgumentException("User UID cannot be blank for update"))
            }
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}