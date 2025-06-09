package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Item

interface ItemRepository {

    suspend fun addItem(item: Item): Result<String>
    suspend fun getItemById(itemId: String): Result<Item?>
    suspend fun getAllItems(limit: Long = 20, lastVisibleItemId: String? = null): Result<List<Item>>
    suspend fun getItemsBySeller(sellerId: String, limit: Long = 20, lastVisibleItemId: String? = null): Result<List<Item>>
    suspend fun updateItem(item: Item): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    suspend fun updateItemStatus(itemId: String, newStatus: String): Result<Unit>
    suspend fun getItemsBySellerId(sellerId: String): Result<List<Item>> // Lấy tất cả item của seller
}