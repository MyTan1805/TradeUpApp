package com.example.tradeup.data.repository

import com.example.tradeup.data.model.config.AppConfig
import com.example.tradeup.data.source.remote.AppConfigSource
import javax.inject.Inject

class AppConfigRepositoryImpl @Inject constructor(
    private val appConfigSource: AppConfigSource
) : AppConfigRepository {

    private var cachedConfig: AppConfig? = null

    override suspend fun getAppConfig(): Result<AppConfig?> {
        if (cachedConfig != null) {
            return Result.success(cachedConfig)
        }
        val result = appConfigSource.getAppConfig()
        result.onSuccess { config ->
            cachedConfig = config
        }
        return result
    }
}