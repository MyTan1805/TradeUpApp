// File: src/main/java/com/example/tradeup/data/repository/AuthRepository.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.CompletableFuture;

public interface AuthRepository {
    FirebaseUser getCurrentUser();

    CompletableFuture<FirebaseUser> registerUser(String email, String password);

    CompletableFuture<FirebaseUser> loginUser(String email, String password);

    void logoutUser();

    CompletableFuture<FirebaseUser> loginWithGoogle(String idToken);

    CompletableFuture<Void> sendPasswordResetEmail(String email);

    CompletableFuture<Void> sendEmailVerification(@NonNull FirebaseUser user);

    CompletableFuture<Void> reauthenticateAndDeleteCurrentUser(String password);
}