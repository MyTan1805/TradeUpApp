// File: src/main/java/com/example/tradeup/data/repository/AdminRepository.java
package com.example.tradeup.data.repository;

import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.data.model.User; // *** SỬA LỖI Ở DÒNG NÀY ***

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AdminRepository {
    CompletableFuture<List<Report>> getPendingReports(long limit);
    CompletableFuture<Void> updateReportStatus(String reportId, String newStatus, String adminNotes);
    CompletableFuture<List<User>> searchUsers(String query);
    CompletableFuture<Void> reactivateUser(String userId);
    CompletableFuture<Void> updateUserRole(String userId, String newRole);
    CompletableFuture<List<Report>> getReportsForContentType(String contentType, long limit);
    CompletableFuture<Void> deleteReviewAndRecalculateUserRating(String ratingId, String ratedUserId, int starsToRemove);
    CompletableFuture<List<Item>> searchAllItems(String query);
}