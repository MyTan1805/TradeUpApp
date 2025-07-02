// File: src/main/java/com/example/tradeup/data/repository/UserRepository.java
package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.User;
import java.util.Map;

public interface UserRepository {
    void createUserProfile(@NonNull User user, Callback<Void> callback);
    void getUserProfile(String uid, Callback<User> callback);
    void updateUserProfile(String uid, Map<String, Object> updates, Callback<Void> callback);
    void deactivateUser(String uid, Callback<Void> callback);
    void deleteUser(String uid, Callback<Void> callback);
}