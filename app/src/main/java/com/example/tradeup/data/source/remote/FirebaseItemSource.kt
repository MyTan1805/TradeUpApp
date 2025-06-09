package com.example.tradeup.data.source.remote

import android.util.Log
import com.example.tradeup.data.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseItemSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirebaseItemSource"
        private const val ITEMS_COLLECTION = "items" // Đảm bảo tên collection đúng
    }


    private val itemsCollection = firestore.collection("items")

    suspend fun getItemsBySellerIdFromSource(sellerId: String): Result<List<Item>> {
        return try {
            Log.d(TAG, "DS: Fetching items for sellerId: $sellerId")
            val snapshot = firestore.collection(ITEMS_COLLECTION)
                .whereEqualTo("sellerId", sellerId)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // Tùy chọn
                .get()
                .await()
            val items = snapshot.toObjects(Item::class.java)
            Log.d(TAG, "DS: Fetched ${items.size} items for sellerId: $sellerId")
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "DS: Error fetching items for sellerId: $sellerId", e)
            Result.failure(e)
        }
    }

    suspend fun addItem(item: Item): Result<String> { // Trả về ID của item mới
        return try {
            val documentReference = itemsCollection.add(item).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(itemId: String): Result<Item?> {
        return try {
            val documentSnapshot = itemsCollection.document(itemId).get().await()
            val item = documentSnapshot.toObject(Item::class.java)?.apply {
                // ID đã được gán tự động bởi toObject nếu field trong model có @DocumentId
                // Nếu không, bạn cần gán thủ công: this.itemId = documentSnapshot.id
            }
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllItems(limit: Long = 20, lastVisibleItemId: String? = null): Result<List<Item>> {
        return try {
            var query: Query = itemsCollection
                .whereEqualTo("status", "available") // Chỉ lấy item available
                .orderBy("createdAt", Query.Direction.DESCENDING) // Sắp xếp mới nhất trước
                .limit(limit)

            if (lastVisibleItemId != null) {
                val lastSnapshot = itemsCollection.document(lastVisibleItemId).get().await()
                query = query.startAfter(lastSnapshot)
            }

            val querySnapshot = query.get().await()
            val items = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Item::class.java)
                // ID đã được gán tự động bởi toObject nếu field trong model có @DocumentId
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemsBySeller(sellerId: String, limit: Long = 20, lastVisibleItemId: String? = null): Result<List<Item>> {
        return try {
            var query: Query = itemsCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastVisibleItemId != null) {
                val lastSnapshot = itemsCollection.document(lastVisibleItemId).get().await()
                query = query.startAfter(lastSnapshot)
            }

            val querySnapshot = query.get().await()
            val items = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Item::class.java)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateItem(item: Item): Result<Unit> {
        return try {
            // Đảm bảo item.itemId không rỗng
            if (item.itemId.isBlank()) {
                return Result.failure(IllegalArgumentException("Item ID cannot be blank for update"))
            }
            itemsCollection.document(item.itemId).set(item).await() // Dùng set để ghi đè toàn bộ, hoặc update cho từng field
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            // Thay vì xóa hẳn, bạn có thể cập nhật status = "deleted"
            // itemsCollection.document(itemId).update("status", "deleted").await()
            // Hoặc xóa hẳn nếu nghiệp vụ yêu cầu:
            itemsCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemStatus(itemId: String, newStatus: String): Result<Unit> {
        return try {
            itemsCollection.document(itemId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bạn có thể thêm các hàm tìm kiếm, lọc phức tạp hơn ở đây
    // Ví dụ: tìm kiếm theo category, tags, khoảng giá, vị trí (cần GeoQueries)
}