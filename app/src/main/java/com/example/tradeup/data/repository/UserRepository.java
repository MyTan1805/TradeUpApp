package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.User; // Model User (Java)

public interface UserRepository {
    void createUserProfile(@NonNull User user, Callback<Void> callback);

    void getUserProfile(String uid, Callback<User> callback); // User có thể null nếu không tìm thấy

    void updateUserProfile(@NonNull User user, Callback<Void> callback);
}