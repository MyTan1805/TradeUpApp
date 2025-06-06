package com.example.tradeup.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.example.tradeup.data.model.Report // Đảm bảo bạn có Report.kt
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseReportSource @Inject constructor(private val firestore: FirebaseFirestore) {

    private val reportsCollection = firestore.collection("reports")

    suspend fun submitReport(report: Report): Result<Unit> {
        return try {
            reportsCollection.add(report).await() // Firestore tự tạo ID
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Các hàm quản lý report (xem, cập nhật status) thường dành cho Admin Panel,
    // không nên có ở client app trừ khi có yêu cầu đặc biệt.
}