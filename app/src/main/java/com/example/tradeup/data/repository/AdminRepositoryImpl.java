// File: src/main/java/com/example/tradeup/data/repository/AdminRepositoryImpl.java
package com.example.tradeup.data.repository;

import com.example.tradeup.core.utils.TaskToFuture;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.data.model.User; // *** SỬA LỖI 1: SỬA LẠI IMPORT CHO ĐÚNG ***
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AdminRepositoryImpl implements AdminRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference reportsCollection;
    private final CollectionReference usersCollection;
    private final CollectionReference itemsCollection;
    private final CollectionReference ratingsCollection;

    @Inject
    public AdminRepositoryImpl(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.reportsCollection = firestore.collection("reports");
        this.usersCollection = firestore.collection("users");
        this.itemsCollection = firestore.collection("items");
        this.ratingsCollection = firestore.collection("ratings");
    }

    @Override
    public CompletableFuture<List<Report>> getPendingReports(long limit) {
        Query query = reportsCollection
                .whereEqualTo("status", "pending_review")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(limit);

        return TaskToFuture.toCompletableFuture(query.get())
                .thenApply(queryDocumentSnapshots ->
                        queryDocumentSnapshots.toObjects(Report.class)
                );
    }

    @Override
    public CompletableFuture<Void> updateReportStatus(String reportId, String newStatus, String adminNotes) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("adminNotes", adminNotes);
        updates.put("resolvedAt", FieldValue.serverTimestamp());

        return TaskToFuture.toCompletableFuture(
                reportsCollection.document(reportId).update(updates)
        );
    }

    @Override
    public CompletableFuture<List<User>> searchUsers(String query) {
        String lowerCaseQuery = query.toLowerCase();

        // *** SỬA Ở ĐÂY: orderBy và tìm kiếm trên trường mới ***
        Query searchQuery = usersCollection
                .orderBy("displayName_lowercase") // Sắp xếp trên trường chữ thường
                .startAt(lowerCaseQuery)
                .endAt(lowerCaseQuery + '\uf8ff')
                .limit(20);

        return TaskToFuture.toCompletableFuture(searchQuery.get())
                .thenApply(queryDocumentSnapshots ->
                        queryDocumentSnapshots.toObjects(User.class)
                );
    }

    @Override
    public CompletableFuture<Void> reactivateUser(String userId) {
        return TaskToFuture.toCompletableFuture(
                usersCollection.document(userId).update("deactivated", false) // Sửa tên trường cho đúng với model
        );
    }

    @Override
    public CompletableFuture<Void> updateUserRole(String userId, String newRole) {
        return TaskToFuture.toCompletableFuture(
                usersCollection.document(userId).update("role", newRole)
        );
    }

    @Override
    public CompletableFuture<List<Item>> searchAllItems(String query) {
        // Tương tự searchUsers, chúng ta cũng cần một trường chữ thường để tìm kiếm hiệu quả
        // Giả sử chúng ta đã tạo trường `searchKeywords` khi đăng tin
        Query searchQuery = itemsCollection
                .whereArrayContains("searchKeywords", query.toLowerCase())
                .limit(20);

        return TaskToFuture.toCompletableFuture(searchQuery.get())
                .thenApply(queryDocumentSnapshots ->
                        queryDocumentSnapshots.toObjects(Item.class)
                );
    }

    @Override
    public CompletableFuture<List<Report>> getReportsForContentType(String contentType, long limit) {
        Query query = reportsCollection
                .whereEqualTo("status", "pending_review")
                .whereEqualTo("reportedContentType", contentType)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(limit);

        return TaskToFuture.toCompletableFuture(query.get())
                .thenApply(queryDocumentSnapshots ->
                        queryDocumentSnapshots.toObjects(Report.class)
                );
    }

    @Override
    public CompletableFuture<Void> deleteReviewAndRecalculateUserRating(String ratingId, String ratedUserId, int starsToRemove) {
        DocumentReference userRef = usersCollection.document(ratedUserId);
        DocumentReference ratingRef = ratingsCollection.document(ratingId);

        return TaskToFuture.toCompletableFuture(
                firestore.runTransaction(transaction -> {
                    DocumentSnapshot userSnapshot = transaction.get(userRef);

                    if (!userSnapshot.exists()) {
                        throw new FirebaseFirestoreException("User not found", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    // Lấy các giá trị cũ
                    long currentTotalRatings = userSnapshot.getLong("totalRatingCount");
                    double currentSumOfStars = userSnapshot.getDouble("sumOfStars");

                    // Tính toán giá trị mới
                    long newTotalRatings = currentTotalRatings - 1;
                    double newSumOfStars = currentSumOfStars - starsToRemove;
                    double newAverageRating = (newTotalRatings > 0) ? (newSumOfStars / newTotalRatings) : 0.0;

                    // Cập nhật user
                    transaction.update(userRef, "totalRatingCount", newTotalRatings);
                    transaction.update(userRef, "sumOfStars", newSumOfStars);
                    transaction.update(userRef, "averageRating", newAverageRating);

                    // Xóa review
                    transaction.delete(ratingRef);

                    return null;
                })
        );
    }
}