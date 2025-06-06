package com.example.tradeup.data.repository

import com.example.tradeup.data.source.remote.FirebaseUserSavedItemsSource // Giả sử bạn sẽ tạo FirebaseUserSavedItemsSource.kt
// import com.example.tradeup.data.model.Item // Nếu cần
import javax.inject.Inject

class UserSavedItemsRepositoryImpl @Inject constructor(
    private val firebaseUserSavedItemsSource: FirebaseUserSavedItemsSource
) : UserSavedItemsRepository {

    override suspend fun saveItem(userId: String, itemId: String): Result<Unit> {
        // return firebaseUserSavedItemsSource.saveItem(userId, itemId)
        TODO("Implement saveItem in FirebaseUserSavedItemsSource and call it here")
    }

    override suspend fun unsaveItem(userId: String, itemId: String): Result<Unit> {
        // return firebaseUserSavedItemsSource.unsaveItem(userId, itemId)
        TODO("Implement unsaveItem in FirebaseUserSavedItemsSource and call it here")
    }

    override suspend fun getSavedItemIds(userId: String): Result<List<String>> {
        // return firebaseUserSavedItemsSource.getSavedItemIds(userId)
        TODO("Implement getSavedItemIds in FirebaseUserSavedItemsSource and call it here")
    }

    override suspend fun isItemSaved(userId: String, itemId: String): Result<Boolean> {
        // return firebaseUserSavedItemsSource.isItemSaved(userId, itemId)
        TODO("Implement isItemSaved in FirebaseUserSavedItemsSource and call it here")
    }
    // ...
}