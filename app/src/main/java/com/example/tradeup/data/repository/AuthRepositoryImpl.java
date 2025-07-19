// File: src/main/java/com/example/tradeup/data/repository/AuthRepositoryImpl.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.TaskToFuture;
import com.example.tradeup.data.source.remote.FirebaseAuthSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuthSource firebaseAuthSource;

    @Inject
    public AuthRepositoryImpl(FirebaseAuthSource firebaseAuthSource) {
        this.firebaseAuthSource = firebaseAuthSource;
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return firebaseAuthSource.getCurrentUser();
    }

    @Override
    public CompletableFuture<FirebaseUser> registerUser(String email, String password) {
        CompletableFuture<FirebaseUser> future = new CompletableFuture<>();
        TaskToFuture.toCompletableFuture(firebaseAuthSource.registerUser(email, password))
                .whenComplete((authResult, throwable) -> {
                    if (throwable != null) {
                        // Xử lý và bọc lỗi bằng một exception có thông điệp rõ ràng hơn
                        if (throwable.getCause() instanceof FirebaseAuthUserCollisionException) {
                            future.completeExceptionally(new Exception("Địa chỉ email này đã được sử dụng."));
                        } else if (throwable.getCause() instanceof FirebaseAuthInvalidCredentialsException) {
                            future.completeExceptionally(new Exception("Định dạng email hoặc mật khẩu không hợp lệ."));
                        } else {
                            future.completeExceptionally(throwable);
                        }
                    } else {
                        future.complete(authResult.getUser());
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<FirebaseUser> loginUser(String email, String password) {
        CompletableFuture<FirebaseUser> future = new CompletableFuture<>();
        TaskToFuture.toCompletableFuture(firebaseAuthSource.loginUser(email, password))
                .whenComplete((authResult, throwable) -> {
                    if (throwable != null) {
                        if (throwable.getCause() instanceof FirebaseAuthInvalidUserException || throwable.getCause() instanceof FirebaseAuthInvalidCredentialsException) {
                            future.completeExceptionally(new Exception("Email hoặc mật khẩu không đúng."));
                        } else {
                            future.completeExceptionally(throwable);
                        }
                    } else {
                        future.complete(authResult.getUser());
                    }
                });
        return future;
    }

    @Override
    public void logoutUser() {
        firebaseAuthSource.logoutUser();
    }

    @Override
    public CompletableFuture<Void> sendPasswordResetEmail(String email) {
        return TaskToFuture.toCompletableFuture(firebaseAuthSource.sendPasswordResetEmail(email));
    }

    @Override
    public CompletableFuture<Void> sendEmailVerification(@NonNull FirebaseUser user) {
        return TaskToFuture.toCompletableFuture(firebaseAuthSource.sendEmailVerification(user));
    }

    @Override
    public CompletableFuture<FirebaseUser> loginWithGoogle(String idToken) {
        return TaskToFuture.toCompletableFuture(firebaseAuthSource.loginWithGoogle(idToken))
                .thenApply(AuthResult::getUser);
    }

    @Override
    public CompletableFuture<Void> reauthenticateAndDeleteCurrentUser(String password) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        TaskToFuture.toCompletableFuture(firebaseAuthSource.reauthenticateAndDeleteCurrentUser(password))
                .whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        if (throwable.getCause() instanceof FirebaseAuthInvalidCredentialsException) {
                            future.completeExceptionally(new Exception("Mật khẩu không đúng. Vui lòng thử lại."));
                        } else {
                            future.completeExceptionally(new Exception("Lỗi xóa tài khoản: " + throwable.getMessage()));
                        }
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }
}