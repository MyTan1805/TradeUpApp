package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Rating;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
        // Tạo các tham chiếu đến các document cần được cập nhật
        final DocumentReference ratingDocRef = ratingsCollection.document(); // Tự tạo ID mới cho rating
        final DocumentReference userDocRef = usersCollection.document(rating.getRatedUserId());
        final DocumentReference transactionDocRef = transactionsCollection.document(rating.getTransactionId());

        // Sử dụng runTransaction để đảm bảo các thao tác đọc-ghi là an toàn (atomic)
        return firestore.runTransaction(transaction -> {
            // === BƯỚC 1: ĐỌC DỮ LIỆU HIỆN TẠI ===
            // Lấy thông tin của người dùng sắp được đánh giá
            DocumentSnapshot userSnapshot = transaction.get(userDocRef);
            if (!userSnapshot.exists()) {
                throw new FirebaseFirestoreException("User to be rated not found.", FirebaseFirestoreException.Code.ABORTED);
            }

            // === BƯỚC 2: TÍNH TOÁN DỮ LIỆU MỚI ===
            // Lấy các giá trị cũ, nếu không có thì mặc định là 0
            long currentTotalRatings = userSnapshot.contains("totalRatingCount") ? userSnapshot.getLong("totalRatingCount") : 0L;
            double currentSumOfStars = userSnapshot.contains("sumOfStars") ? userSnapshot.getDouble("sumOfStars") : 0.0;

            // Tính toán các giá trị mới
            long newTotalRatings = currentTotalRatings + 1;
            double newSumOfStars = currentSumOfStars + rating.getStars();
            // Tránh chia cho 0
            double newAverageRating = (newTotalRatings > 0) ? (newSumOfStars / newTotalRatings) : 0.0;

            // === BƯỚC 3: THỰC HIỆN CÁC THAO TÁC GHI DỮ LIỆU ===
            // 1. Ghi document Rating mới
            transaction.set(ratingDocRef, rating);

            // 2. Cập nhật User document
            transaction.update(userDocRef, "averageRating", newAverageRating);
            transaction.update(userDocRef, "totalRatingCount", newTotalRatings);
            transaction.update(userDocRef, "sumOfStars", newSumOfStars);

            // 3. Cập nhật Transaction document
            // Xác định xem người đánh giá là người mua hay người bán để cập nhật đúng trường
            boolean isRaterTheBuyer = rating.getRaterUserId().equals(transaction.get(transactionDocRef).getString("buyerId"));
            if (isRaterTheBuyer) {
                transaction.update(transactionDocRef, "ratingGivenByBuyer", true);
            } else {
                transaction.update(transactionDocRef, "ratingGivenBySeller", true);
            }

            // runTransaction yêu cầu trả về một giá trị, có thể là null
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