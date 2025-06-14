package com.example.tradeup.data.repository;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.config.AppConfig;

public interface AppConfigRepository {
    void getAppConfig(Callback<AppConfig> callback);
}