package com.example.tradeup.data.repository

import com.example.tradeup.data.model.config.AppConfig // Đã đổi

interface AppConfigRepository {
    suspend fun getAppConfig(): Result<AppConfig?>
}
