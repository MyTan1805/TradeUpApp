package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.example.tradeup.data.model.config.AppConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppConfigSource @Inject constructor(private val firestore: FirebaseFirestore) {

    suspend fun getAppConfig(): Result<AppConfig?> {
        return try {
            val documentSnapshot = firestore.collection("appConfig").document("global").get().await()
            val config = documentSnapshot.toObject(AppConfig::class.java)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}