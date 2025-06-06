package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Report // Giả sử bạn có model Report.kt

interface ReportRepository {
    suspend fun submitReport(report: Report): Result<Unit>
}