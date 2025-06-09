package com.example.tradeup.data.repository

import android.util.Log
import com.example.tradeup.data.model.Item
import com.example.tradeup.data.source.remote.FirebaseItemSource
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val firebaseItemSource: FirebaseItemSource
) : ItemRepository {
    companion object {
        private const val ITEMS_COLLECTION = "items" // << ĐỊNH NGHĨA TÊN COLLECTION Ở ĐÂY
    }

    override suspend fun getItemsBySellerId(sellerId: String): Result<List<Item>> {
        Log.d("ItemRepositoryImpl", "getItemsBySellerId called for sellerId: $sellerId")
        return firebaseItemSource.getItemsBySellerIdFromSource(sellerId)
    }

    override suspend fun addItem(item: Item): Result<String> {
        return firebaseItemSource.addItem(item)
    }

    override suspend fun getItemById(itemId: String): Result<Item?> {
        return firebaseItemSource.getItemById(itemId)
    }

    override suspend fun getAllItems(limit: Long, lastVisibleItemId: String?): Result<List<Item>> {
        return firebaseItemSource.getAllItems(limit, lastVisibleItemId)
    }

    override suspend fun getItemsBySeller(sellerId: String, limit: Long, lastVisibleItemId: String?): Result<List<Item>> {
        Log.d("ItemRepositoryImpl", "getItemsBySeller (paginated) called for sellerId: $sellerId")
        return firebaseItemSource.getItemsBySeller(sellerId, limit, lastVisibleItemId)
    }

    override suspend fun updateItem(item: Item): Result<Unit> {
        return firebaseItemSource.updateItem(item)
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> {
        return firebaseItemSource.deleteItem(itemId)
    }

    override suspend fun updateItemStatus(itemId: String, newStatus: String): Result<Unit> {
        return firebaseItemSource.updateItemStatus(itemId, newStatus)
    }

}