// File: src/main/java/com/example/tradeup/data/repository/UserRepository.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.User;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {
    CompletableFuture<Void> createUserProfile(@NonNull User user);
    CompletableFuture<User> getUserProfile(String uid);
    CompletableFuture<Void> updateUserProfile(String uid, Map<String, Object> updates);
    CompletableFuture<Void> deactivateUser(String uid);
    CompletableFuture<Void> deleteUser(String uid);
    CompletableFuture<Void> blockUser(@NonNull String currentUserId, @NonNull String userToBlockId);
}