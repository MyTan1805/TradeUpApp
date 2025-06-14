package com.example.tradeup.data.source.remote;

import com.example.tradeup.core.utils.Callback; // Import Callback interface
import com.example.tradeup.data.model.config.AppConfig; // Import model AppConfig của bạn (phải là class Java)
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Inject;

public class AppConfigSource {

    private final FirebaseFirestore firestore;

    @Inject
    public AppConfigSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void getAppConfig(Callback<AppConfig> callback) {
        firestore.collection("appConfig").document("global").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AppConfig config = documentSnapshot.toObject(AppConfig.class);
                        if (config != null) {
                            callback.onSuccess(config);
                        } else {
                            // Trường hợp toObject trả về null (hiếm nếu document tồn tại và khớp model)
                            callback.onFailure(new Exception("Failed to parse AppConfig from Firestore document."));
                        }
                    } else {
                        // Document "global" không tồn tại
                        callback.onFailure(new Exception("AppConfig 'global' document does not exist."));
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }
}