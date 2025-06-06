package com.example.tradeup.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// THAY THẾ CÁC GIÁ TRỊ NÀY BẰNG THÔNG TIN CỦA BẠN
private val CLOUDINARY_CLOUD_NAME = "dhv2ihonf"
private val CLOUDINARY_API_KEY = "389774338928861" // API Key, không phải API Secret
private val CLOUDINARY_UPLOAD_PRESET = "user" // Ví dụ: "tradeup_unsigned_preset"

suspend fun uploadImageDirectlyToCloudinary(
    context: Context,
    imageUri: Uri
): String? { // Trả về URL ảnh hoặc null nếu lỗi

    if (CLOUDINARY_CLOUD_NAME == "YOUR_CLOUD_NAME" ||
        CLOUDINARY_API_KEY == "CLOUDINARY_API_KEY" ||
        CLOUDINARY_UPLOAD_PRESET == "CLOUDINARY_UPLOAD_PRESET") {
        println("Cloudinary credentials not set. Please update constants.")
        return null
    }

    return withContext(Dispatchers.IO) {
        val tempFile: File? = try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val fileName = "upload_temp_${System.currentTimeMillis()}.${context.contentResolver.getType(imageUri)?.split('/')?.lastOrNull() ?: "jpg"}"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: IOException) {
            println("Error creating temp file: ${e.message}")
            return@withContext null
        }

        if (tempFile == null || !tempFile.exists()) {
            println("Temp file not created or does not exist")
            return@withContext null
        }

        try {
            val client = OkHttpClient()
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, tempFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("api_key", CLOUDINARY_API_KEY) // API Key cần cho unsigned upload nếu không dùng SDK client
                // .addFormDataPart("timestamp", (System.currentTimeMillis() / 1000L).toString()) // Timestamp có thể cần nếu preset yêu cầu signed, nhưng với unsigned thường không cần
                // .addFormDataPart("folder", "your_folder_name") // Có thể ghi đè folder ở đây nếu muốn
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload") // Hoặc /video/upload nếu là video
                .post(requestBody)
                .build()

            println("Uploading to Cloudinary: ${request.url}")
            val response = client.newCall(request).execute()
            tempFile.delete()

            if (!response.isSuccessful) {
                println("Cloudinary Upload failed: ${response.code} ${response.message}")
                val errorBody = response.body?.string()
                println("Error Body: $errorBody")
                // Phân tích lỗi từ Cloudinary nếu có
                try {
                    val jsonError = JSONObject(errorBody.toString())
                    val errorMessage = jsonError.optJSONObject("error")?.optString("message", "Unknown error")
                    println("Cloudinary error message: $errorMessage")
                } catch (e: Exception) { /* Bỏ qua lỗi parse JSON */ }
                return@withContext null
            }

            val responseBody = response.body?.string()
            if (responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val imageUrl = jsonResponse.optString("secure_url", null)
                // val publicId = jsonResponse.optString("public_id", null)
                println("Cloudinary Upload successful! Image URL: $imageUrl")
                return@withContext imageUrl
            } else {
                println("Empty response body from Cloudinary")
                return@withContext null
            }

        } catch (e: Exception) {
            println("Error during Cloudinary direct upload: ${e.message}")
            if (tempFile.exists()) tempFile.delete()
            return@withContext null
        }
    }
}