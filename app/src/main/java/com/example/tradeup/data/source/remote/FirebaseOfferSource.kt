package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.tradeup.data.model.Offer // Đảm bảo bạn có Offer.kt
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseOfferSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val offersCollection = firestore.collection("offers")

    suspend fun createOffer(offer: Offer): Result<String> { // Trả về offerId
        return try {
            val documentReference = offersCollection.add(offer).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOfferById(offerId: String): Result<Offer?> {
        return try {
            val documentSnapshot = offersCollection.document(offerId).get().await()
            val offer = documentSnapshot.toObject(Offer::class.java)
            Result.success(offer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOffersForItem(itemId: String): Result<List<Offer>> {
        return try {
            val querySnapshot = offersCollection
                .whereEqualTo("itemId", itemId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val offers = querySnapshot.documents.mapNotNull { it.toObject(Offer::class.java) }
            Result.success(offers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOffersByBuyer(buyerId: String): Result<List<Offer>> {
        return try {
            val querySnapshot = offersCollection
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val offers = querySnapshot.documents.mapNotNull { it.toObject(Offer::class.java) }
            Result.success(offers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOfferStatus(
        offerId: String,
        newStatus: String,
        counterPrice: Double? = null, // Cho trường hợp "countered"
        counterMessage: String? = null // Cho trường hợp "countered"
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            if (newStatus == "countered" && counterPrice != null) {
                updates["counterOfferPrice"] = counterPrice
                counterMessage?.let { updates["counterOfferMessage"] = it }
            }
            // Thêm logic cho các status khác nếu cần cập nhật field riêng
            offersCollection.document(offerId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bạn có thể cần thêm hàm để lấy offer mà người bán nhận được
    suspend fun getOffersForSeller(sellerId: String): Result<List<Offer>> {
        return try {
            val querySnapshot = offersCollection
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val offers = querySnapshot.documents.mapNotNull { it.toObject(Offer::class.java) }
            Result.success(offers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}