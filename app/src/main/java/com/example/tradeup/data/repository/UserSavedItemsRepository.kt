package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Item // Có thể bạn muốn trả về danh sách Item thay vì chỉ ID

interface UserSavedItemsRepository {
    suspend fun saveItem(userId: String, itemId: String): Result<Unit>
    suspend fun unsaveItem(userId: String, itemId: String): Result<Unit>
    suspend fun getSavedItemIds(userId: String): Result<List<String>> // Hoặc Result<List<Item>> nếu muốn lấy cả object Item
    suspend fun isItemSaved(userId: String, itemId: String): Result<Boolean>
}