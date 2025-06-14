package com.example.tradeup.data.repository;

import androidx.annotation.NonNull; // Để chỉ rõ tham số không được null
import com.example.tradeup.core.utils.Callback; // Import Callback
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task; // Import Task
import com.google.firebase.auth.AuthResult; // Import AuthResult

public interface AuthRepository {
    FirebaseUser getCurrentUser(); // Đồng bộ, trả về trực tiếp

    void registerUser(String email, String password, Callback<FirebaseUser> callback); // Thay Result bằng Callback

    void loginUser(String email, String password, Callback<FirebaseUser> callback); // Thay Result bằng Callback

    void logoutUser(); // Đồng bộ

    void sendPasswordResetEmail(String email, Callback<Void> callback); // Thay Result bằng Callback

    void sendEmailVerification(@NonNull FirebaseUser user, Callback<Void> callback); // Thay Result bằng Callback
}