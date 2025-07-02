package com.example.tradeup.core.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CloudinaryUploader {

    private static final String TAG = "CloudinaryUploader";

    // << FIX 1: Tạo Handler để đảm bảo callback được gọi trên Main Thread >>
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public interface CloudinaryUploadCallback {
        void onSuccess(@NonNull String imageUrl);
        void onFailure(@NonNull Exception e);
        void onErrorResponse(int code, @Nullable String errorMessage);
    }

    public static void uploadImageDirectlyToCloudinary(
            @NonNull Context context,
            @NonNull Uri imageUri,
            @NonNull CloudinaryUploadCallback callback) {

        // Bước 1: Đọc các key từ string resources
        final String cloudName = context.getString(R.string.cloudinary_cloud_name);
        final String apiKey = context.getString(R.string.cloudinary_api_key);
        final String uploadPreset = context.getString(R.string.cloudinary_upload_preset);

        // Bước 2: Kiểm tra xem các key đã được cấu hình đúng chưa
        if ("DEFAULT_CLOUD_NAME".equals(cloudName) || cloudName.isEmpty() ||
                "DEFAULT_API_KEY".equals(apiKey) || apiKey.isEmpty() ||
                "DEFAULT_UPLOAD_PRESET".equals(uploadPreset) || uploadPreset.isEmpty()) {

            Log.e(TAG, "Cloudinary credentials not set. Please update values in local.properties");
            // Gọi onFailure trên main thread ngay lập tức
            postToMainThread(() -> callback.onFailure(new IllegalStateException("Cloudinary credentials not configured.")));
            return;
        }

        // << FIX 2: Tạo ExecutorService mới cho mỗi lần upload để tránh rò rỉ >>
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            File tempFile = null;
            try {
                // Tạo file tạm từ Uri. Logic này được giữ nguyên.
                tempFile = createTempFileFromUri(context, imageUri);

                // Upload lên Cloudinary
                performUpload(tempFile, context, imageUri, cloudName, apiKey, uploadPreset, callback);

            } catch (Exception e) {
                Log.e(TAG, "Error during Cloudinary direct upload", e);
                postToMainThread(() -> callback.onFailure(e));
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        });
    }

    private static void performUpload(File tempFile, Context context, Uri imageUri, String cloudName, String apiKey, String uploadPreset, CloudinaryUploadCallback callback) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String mimeType = context.getContentResolver().getType(imageUri);
        if (mimeType == null) {
            mimeType = "image/jpeg"; // Mặc định
        }

        RequestBody fileBody = RequestBody.create(tempFile, MediaType.parse(mimeType));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.getName(), fileBody)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("api_key", apiKey)
                .build();

        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload")
                .post(requestBody)
                .build();

        Log.d(TAG, "Uploading to Cloudinary: " + request.url());
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response, callback);
            } else {
                handleSuccessResponse(response, callback);
            }
        }
    }

    private static void handleSuccessResponse(Response response, CloudinaryUploadCallback callback) {
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                postToMainThread(() -> callback.onFailure(new IOException("Empty response body from Cloudinary.")));
                return;
            }
            String responseBodyString = responseBody.string();
            JSONObject jsonResponse = new JSONObject(responseBodyString);
            String imageUrl = jsonResponse.optString("secure_url", null);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Log.d(TAG, "Cloudinary Upload successful! Image URL: " + imageUrl);
                postToMainThread(() -> callback.onSuccess(imageUrl));
            } else {
                Log.e(TAG, "Empty or null secure_url in Cloudinary response: " + responseBodyString);
                postToMainThread(() -> callback.onFailure(new IOException("Failed to get image URL from Cloudinary response.")));
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error parsing Cloudinary success response", e);
            postToMainThread(() -> callback.onFailure(e));
        }
    }

    private static void handleErrorResponse(Response response, CloudinaryUploadCallback callback) {
        String errorBodyString = null;
        String cloudinaryErrorMessage = "Unknown Cloudinary error";
        try (ResponseBody errorBody = response.body()) {
            if (errorBody != null) {
                errorBodyString = errorBody.string();
                JSONObject jsonError = new JSONObject(errorBodyString);
                JSONObject errorObj = jsonError.optJSONObject("error");
                if (errorObj != null) {
                    cloudinaryErrorMessage = errorObj.optString("message", "Unknown Cloudinary error");
                }
            }
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error parsing Cloudinary error response", e);
        }
        Log.e(TAG, "Cloudinary Upload failed: " + response.code() + " " + response.message() + " | Body: " + errorBodyString);

        final String finalCloudinaryErrorMessage = cloudinaryErrorMessage;
        final int responseCode = response.code();
        postToMainThread(() -> callback.onErrorResponse(responseCode, finalCloudinaryErrorMessage));
    }


    private static File createTempFileFromUri(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Failed to open input stream from Uri.");
        }

        String extension = getFileExtension(context, imageUri);
        String fileName = "upload_temp_" + System.currentTimeMillis() + "." + extension;
        File tempFile = new File(context.getCacheDir(), fileName);

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4 * 1024]; // 4K buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        } finally {
            inputStream.close();
        }

        if (!tempFile.exists() || tempFile.length() == 0) {
            throw new IOException("Temp file not created or is empty.");
        }
        return tempFile;
    }


    private static String getFileExtension(Context context, Uri uri) {
        String extension;
        if (uri.getScheme() != null && uri.getScheme().equals(android.content.ContentResolver.SCHEME_CONTENT)) {
            android.webkit.MimeTypeMap mime = android.webkit.MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(Objects.requireNonNull(uri.getPath()))).toString());
        }
        return (extension == null || extension.isEmpty()) ? "jpg" : extension;
    }

    // << FIX 3: Sửa lại tên hàm cho rõ ràng và sử dụng Handler >>
    private static void postToMainThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
}