package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Rating; // Model Rating (Java)
import com.example.tradeup.data.model.Transaction; // Model Transaction (Java) - cần để lấy buyerId/sellerId
import com.example.tradeup.data.model.User; // Model User (Java) - cần để cập nhật rating
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.TransactionOptions; // TransactionOptions nếu cần
import com.google.firebase.firestore.WriteBatch; // Không dùng WriteBatch trong Transaction function của Firestore

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

    public Task<Void> submitRating(@NonNull Rating rating) {
        // Firestore Transaction trong Java trả về Task<TResult>
        return firestore.runTransaction(transaction -> {
            DocumentReference ratingDocRef = ratingsCollection.document(); // Tạo ID mới cho rating
            // Firestore tự xử lý @ServerTimestamp khi dùng POJO
            transaction.set(ratingDocRef, rating);

            // Cập nhật averageRating cho ratedUserId
            DocumentReference userDocRef = usersCollection.document(rating.getRatedUserId());
            DocumentSnapshot userSnapshot = transaction.get(userDocRef);

            Long currentTotalRatings = userSnapshot.getLong("totalRatingCount");
            if (currentTotalRatings == null) currentTotalRatings = 0L;

            Double currentSumOfStars = userSnapshot.getDouble("sumOfStars");
            if (currentSumOfStars == null) currentSumOfStars = 0.0;

            long newTotalRatings = currentTotalRatings + 1;
            double newSumOfStars = currentSumOfStars + rating.getStars();
            double newAverageRating = (newTotalRatings > 0) ? newSumOfStars / newTotalRatings : 0.0;

            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("averageRating", newAverageRating);
            userUpdates.put("totalRatingCount", newTotalRatings);
            userUpdates.put("sumOfStars", newSumOfStars);
            userUpdates.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            transaction.update(userDocRef, userUpdates);

            // Cập nhật trạng thái đã đánh giá trong transaction
            DocumentReference transactionDocRef = transactionsCollection.document(rating.getTransactionId());
            DocumentSnapshot transactionSnapshot = transaction.get(transactionDocRef); // Lấy transaction document

            // Bạn cần có model Transaction.java hoặc lấy field trực tiếp
            String buyerId = transactionSnapshot.getString("buyerId");
            String sellerId = transactionSnapshot.getString("sellerId");

            if (Objects.equals(rating.getRaterUserId(), buyerId)) {
                transaction.update(transactionDocRef, "ratingGivenByBuyer", true);
            } else if (Objects.equals(rating.getRaterUserId(), sellerId)) {
                transaction.update(transactionDocRef, "ratingGivenBySeller", true);
            }
            // Transaction function trong Java cần trả về một kết quả (có thể là null)
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