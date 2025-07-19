// File: src/main/java/com/example/tradeup/data/repository/UserRepositoryImpl.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.TaskToFuture;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.source.remote.FirestoreUserSource;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {

    private final FirestoreUserSource firestoreUserSource;

    @Inject
    public UserRepositoryImpl(FirestoreUserSource firestoreUserSource) {
        this.firestoreUserSource = firestoreUserSource;
    }

    @Override
    public CompletableFuture<Void> createUserProfile(@NonNull User user) {
        return TaskToFuture.toCompletableFuture(firestoreUserSource.createUserProfile(user));
    }

    @Override
    public CompletableFuture<User> getUserProfile(String uid) {
        CompletableFuture<User> future = new CompletableFuture<>();
        TaskToFuture.toCompletableFuture(firestoreUserSource.getUserProfile(uid))
                .whenComplete((user, throwable) -> {
                    if (throwable != null) {
                        future.completeExceptionally(throwable);
                    } else if (user == null) {
                        // Trả về null để ViewModel xử lý logic tạo user mới
                        future.complete(null);
                    } else {
                        future.complete(user);
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<Void> updateUserProfile(String uid, Map<String, Object> updates) {
        return TaskToFuture.toCompletableFuture(firestoreUserSource.updateUserFields(uid, updates));
    }

    @Override
    public CompletableFuture<Void> deactivateUser(String uid) {
        return TaskToFuture.toCompletableFuture(firestoreUserSource.deactivateUser(uid));
    }

    @Override
    public CompletableFuture<Void> deleteUser(String uid) {
        return TaskToFuture.toCompletableFuture(firestoreUserSource.deleteUser(uid));
    }

    @Override
    public CompletableFuture<Void> blockUser(@NonNull String currentUserId, @NonNull String userToBlockId) {
        return TaskToFuture.toCompletableFuture(firestoreUserSource.blockUser(currentUserId, userToBlockId));
    }
}