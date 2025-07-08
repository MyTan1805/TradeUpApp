package com.example.tradeup.core.utils;

import android.util.Log;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FcmTokenUtil {
    private static final String TAG = "FcmTokenUtil";

    public static void saveFcmTokenToFirestore(String userId, String token) {
        if (userId == null || token == null) {
            Log.w(TAG, "User ID or token is null, skipping save.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmTokens", FieldValue.arrayUnion(token));

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "FCM token updated successfully for user: " + userId))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating FCM token", e));
    }
}