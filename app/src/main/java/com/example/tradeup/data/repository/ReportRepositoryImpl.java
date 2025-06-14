package com.example.tradeup.data.repository;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.data.source.remote.FirebaseReportSource;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReportRepositoryImpl implements ReportRepository {

    private final FirebaseReportSource firebaseReportSource;

    @Inject
    public ReportRepositoryImpl(FirebaseReportSource firebaseReportSource) {
        this.firebaseReportSource = firebaseReportSource;
    }

    @Override
    public void submitReport(@NonNull Report report, final Callback<Void> callback) {
        firebaseReportSource.submitReport(report)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}