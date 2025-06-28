package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Rating;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;

public class FirebaseRatingSource {

    private final FirebaseFirestore firestore;
    private final CollectionReference ratingsCollection;
    private final CollectionReference usersCollection;
    private final CollectionReference transactionsCollection;

    @Inject
    public FirebaseRatingSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.ratingsCollection = firestore.collection("ratings");
        this.usersCollection = firestore.collection("users");
        this.transactionsCollection = firestore.collection("transactions");
    }

    /**
     * Gửi đánh giá và cập nhật các thông tin liên quan trong một transaction duy nhất.
     * 1. Tạo một document Rating mới.
     * 2. Cập nhật điểm trung bình, tổng số sao và tổng số lượt đánh giá cho người được rated.
     * 3. Đánh dấu là đã đưa ra đánh giá trong document Transaction tương ứng.
     * @param rating Đối tượng Rating cần lưu.
     * @return Task<Void> cho biết transaction thành công hay thất bại.
     */
    public Task<Void> submitRating(@NonNull Rating rating) {
        return firestore.runTransaction(transaction -> {
            // 1. Tạo một document Rating mới
            DocumentReference ratingDocRef = ratingsCollection.document();
            transaction.set(ratingDocRef, rating);

            // 2. Cập nhật thông tin cho người được đánh giá (ratedUserId)
            DocumentReference userDocRef = usersCollection.document(rating.getRatedUserId());
            DocumentSnapshot userSnapshot = transaction.get(userDocRef);

            // Lấy các giá trị hiện tại, nếu không có thì mặc định là 0
            Long currentTotalRatings = userSnapshot.getLong("totalRatingCount");
            if (currentTotalRatings == null) currentTotalRatings = 0L;

            Double currentSumOfStars = userSnapshot.getDouble("sumOfStars");
            if (currentSumOfStars == null) currentSumOfStars = 0.0;

            // Tính toán các giá trị mới
            long newTotalRatings = currentTotalRatings + 1;
            double newSumOfStars = currentSumOfStars + rating.getStars();
            double newAverageRating = (newTotalRatings > 0) ? (newSumOfStars / newTotalRatings) : 0.0;

            // Tạo map để cập nhật user
            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("averageRating", newAverageRating);
            userUpdates.put("totalRatingCount", newTotalRatings);
            userUpdates.put("sumOfStars", newSumOfStars);
            userUpdates.put("updatedAt", FieldValue.serverTimestamp());
            transaction.update(userDocRef, userUpdates);

            // 3. Cập nhật trạng thái đã đánh giá trong document Transaction
            DocumentReference transactionDocRef = transactionsCollection.document(rating.getTransactionId());
            DocumentSnapshot transactionSnapshot = transaction.get(transactionDocRef);

            String buyerId = transactionSnapshot.getString("buyerId");

            // Xác định người đánh giá là người mua hay người bán để cập nhật đúng trường
            if (Objects.equals(rating.getRaterUserId(), buyerId)) {
                transaction.update(transactionDocRef, "ratingGivenByBuyer", true);
            } else { // Người đánh giá là người bán
                transaction.update(transactionDocRef, "ratingGivenBySeller", true);
            }

            // Transaction trong Java cần trả về một giá trị, có thể là null
            return null;
        });
    }

    public Task<List<Rating>> getRatingsForUser(String userId, long limit) {
        return ratingsCollection
                .whereEqualTo("ratedUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return Objects.requireNonNull(task.getResult()).toObjects(Rating.class);
                });
    }

    public Task<Rating> getRatingForTransaction(String transactionId, String raterId) {
        return ratingsCollection
                .whereEqualTo("transactionId", transactionId)
                .whereEqualTo("raterUserId", raterId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        return querySnapshot.getDocuments().get(0).toObject(Rating.class);
                    }
                    return null; // Trả về null nếu không tìm thấy
                });
    }
}