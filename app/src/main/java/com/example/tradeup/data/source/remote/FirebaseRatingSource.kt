package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.tradeup.data.model.Rating // Đảm bảo bạn có Rating.kt
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRatingSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val ratingsCollection = firestore.collection("ratings")
    private val usersCollection = firestore.collection("users") // Để cập nhật averageRating
    private val transactionsCollection = firestore.collection("transactions") // Để cập nhật ratingGivenBy...


    suspend fun submitRating(rating: Rating): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val ratingDocRef = ratingsCollection.document() // Tạo ID mới cho rating
                transaction.set(ratingDocRef, rating)

                // Cập nhật averageRating cho ratedUserId (Cần tính toán lại trung bình)
                // Đây là một tác vụ phức tạp, thường được thực hiện tốt hơn bằng Cloud Function
                // Để đơn giản ở client: lấy tất cả rating của user, tính lại trung bình, rồi update.
                // Hoặc bạn có thể dùng FieldValue.increment để cập nhật totalStars và totalRatingCount rồi tính ở client.
                // Ví dụ đơn giản (không tối ưu cho nhiều ghi đồng thời):
                val userDocRef = usersCollection.document(rating.ratedUserId)
                val userSnapshot = transaction.get(userDocRef)
                val currentTotalRatings = userSnapshot.getLong("totalRatingCount") ?: 0L // Cần thêm field này vào User model
                val currentSumOfStars = userSnapshot.getDouble("sumOfStars") ?: 0.0 // Cần thêm field này vào User model

                val newTotalRatings = currentTotalRatings + 1
                val newSumOfStars = currentSumOfStars + rating.stars
                val newAverageRating = if (newTotalRatings > 0) newSumOfStars / newTotalRatings else 0.0

                transaction.update(userDocRef, mapOf(
                    "averageRating" to newAverageRating,
                    "totalRatingCount" to newTotalRatings, // Cập nhật các field giả định này
                    "sumOfStars" to newSumOfStars         // Cập nhật các field giả định này
                ))

                // Cập nhật trạng thái đã đánh giá trong transaction
                val transactionDocRef = transactionsCollection.document(rating.transactionId)
                if (rating.raterUserId == transaction.get(transactionDocRef).getString("buyerId")) {
                    transaction.update(transactionDocRef, "ratingGivenByBuyer", true)
                } else if (rating.raterUserId == transaction.get(transactionDocRef).getString("sellerId")) {
                    transaction.update(transactionDocRef, "ratingGivenBySeller", true)
                }
                null // runTransaction yêu cầu trả về một giá trị
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatingsForUser(userId: String, limit: Long = 10): Result<List<Rating>> {
        return try {
            val querySnapshot = ratingsCollection
                .whereEqualTo("ratedUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val ratings = querySnapshot.documents.mapNotNull { it.toObject(Rating::class.java) }
            Result.success(ratings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatingForTransaction(transactionId: String, raterId: String): Result<Rating?> {
        return try {
            val querySnapshot = ratingsCollection
                .whereEqualTo("transactionId", transactionId)
                .whereEqualTo("raterUserId", raterId)
                .limit(1)
                .get()
                .await()
            val rating = querySnapshot.documents.firstOrNull()?.toObject(Rating::class.java)
            Result.success(rating)
        } catch (e: Exception){
        Result.failure(e)
    }
}
}