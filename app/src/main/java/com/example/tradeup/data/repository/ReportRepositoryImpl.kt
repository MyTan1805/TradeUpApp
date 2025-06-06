package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Report
import com.example.tradeup.data.source.remote.FirebaseReportSource // Giả sử bạn sẽ tạo FirebaseReportSource.kt
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val firebaseReportSource: FirebaseReportSource
) : ReportRepository {

    override suspend fun submitReport(report: Report): Result<Unit> {
        // return firebaseReportSource.submitReport(report)
        TODO("Implement submitReport in FirebaseReportSource and call it here")
    }
}