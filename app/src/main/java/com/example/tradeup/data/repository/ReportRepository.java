package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Report; // Model Report (Java)

public interface ReportRepository {
    void submitReport(@NonNull Report report, Callback<Void> callback);
}