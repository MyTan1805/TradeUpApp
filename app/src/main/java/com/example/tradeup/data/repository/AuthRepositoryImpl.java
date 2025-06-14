package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.source.remote.FirebaseAuthSource;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

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
    public void registerUser(String email, String password, final Callback<FirebaseUser> callback) {
        firebaseAuthSource.registerUser(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        // Trường hợp này hiếm khi xảy ra nếu task thành công
                        callback.onFailure(new Exception("Registration successful but user is null."));
                    }
                })
                .addOnFailureListener(e -> {
                    // Bạn có thể xử lý các exception cụ thể ở đây để đưa ra thông báo lỗi tốt hơn
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        callback.onFailure(new Exception("Địa chỉ email này đã được sử dụng."));
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onFailure(new Exception("Định dạng email hoặc mật khẩu không hợp lệ."));
                    }
                    else {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void loginUser(String email, String password, final Callback<FirebaseUser> callback) {
        firebaseAuthSource.loginUser(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("Login successful but user is null."));
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onFailure(new Exception("Email hoặc mật khẩu không đúng."));
                    } else {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void logoutUser() {
        firebaseAuthSource.logoutUser();
    }

    @Override
    public void sendPasswordResetEmail(String email, final Callback<Void> callback) {
        firebaseAuthSource.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null)) // Tham số của onSuccess cho Void là null
                .addOnFailureListener(callback::onFailure); // Tham chiếu phương thức
    }

    @Override
    public void sendEmailVerification(@NonNull FirebaseUser user, final Callback<Void> callback) {
        firebaseAuthSource.sendEmailVerification(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}