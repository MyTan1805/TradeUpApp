// File: src/main/java/com/example/tradeup/data/repository/StorageRepository.java
package com.example.tradeup.data.repository;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class StorageRepository {

    private final Context appContext;

    @Inject
    public StorageRepository(@ApplicationContext Context context) {
        this.appContext = context;
    }

    /**
     * Tải ảnh đại diện lên Cloudinary bằng cách gọi đến CloudinaryUploader.
     * @param userId      ID người dùng (không dùng trong logic này nhưng giữ lại cho nhất quán).
     * @param imageUri    URI của file ảnh.
     * @param callback    Callback để trả về kết quả cho ViewModel.
     */
    public void uploadProfilePicture(String userId, Uri imageUri, @NonNull final Callback<String> callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Image URI cannot be null."));
            return;
        }

        CloudinaryUploader.uploadImageDirectlyToCloudinary(
                appContext,
                imageUri,
                new CloudinaryUploader.CloudinaryUploadCallback() {
                    @Override
                    public void onSuccess(@NonNull String imageUrl) {
                        callback.onSuccess(imageUrl);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }

                    @Override
                    public void onErrorResponse(int code, @Nullable String errorMessage) {
                        String message = "Cloudinary Error (" + code + "): " + (errorMessage != null ? errorMessage : "Unknown error");
                        callback.onFailure(new java.io.IOException(message));
                    }
                }
        );
    }
}