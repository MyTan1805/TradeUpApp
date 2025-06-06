package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tradeup.data.model.UserSavedItems // Đảm bảo bạn có UserSavedItems.kt
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserSavedItemsSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val savedItemsCollection = firestore.collection("userSavedItems")

    suspend fun saveItem(userId: String, itemId: String): Result<Unit> {
        return try {
            val docRef = savedItemsCollection.document(userId)
            // Dùng arrayUnion để thêm itemId vào mảng itemIds một cách an toàn, tránh trùng lặp
            docRef.update("itemIds", FieldValue.arrayUnion(itemId),
                "updatedAt", FieldValue.serverTimestamp())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            // Xử lý trường hợp document chưa tồn tại nếu update thất bại
            if (e is com.google.firebase.firestore.FirebaseFirestoreException &&
                e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                return try {
                    savedItemsCollection.document(userId)
                        .set(UserSavedItems(userId = userId, itemIds = listOf(itemId))) // timestamp sẽ tự động bởi model
                        .await()
                    Result.success(Unit)
                } catch (setException: Exception) {
                    Result.failure(setException)
                }
            }
            Result.failure(e)
        }
    }

    suspend fun unsaveItem(userId: String, itemId: String): Result<Unit> {
        return try {
            savedItemsCollection.document(userId)
                .update("itemIds", FieldValue.arrayRemove(itemId),
                    "updatedAt", FieldValue.serverTimestamp())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSavedItemIds(userId: String): Result<List<String>> {
        return try {
            val documentSnapshot = savedItemsCollection.document(userId).get().await()
            val savedItemsDoc = documentSnapshot.toObject(UserSavedItems::class.java)
            Result.success(savedItemsDoc?.itemIds ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isItemSaved(userId: String, itemId: String): Result<Boolean> {
        return try {
            val documentSnapshot = savedItemsCollection.document(userId).get().await()
            val savedItemsDoc = documentSnapshot.toObject(UserSavedItems::class.java)
            Result.success(savedItemsDoc?.itemIds?.contains(itemId) ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}