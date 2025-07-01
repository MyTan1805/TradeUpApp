// File: src/main/java/com/example/tradeup/ui/report/ReportViewModel.java

package com.example.tradeup.ui.report;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ReportRepository;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReportViewModel extends ViewModel {

    private final ReportRepository reportRepository;
    private final AuthRepository authRepository;
    private final AppConfigRepository appConfigRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<Boolean>> _submitSuccess = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getSubmitSuccess() { return _submitSuccess; }

    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();
    public LiveData<AppConfig> getAppConfig() { return _appConfig; }

    @Inject
    public ReportViewModel(ReportRepository reportRepository, AuthRepository authRepository, AppConfigRepository appConfigRepository) {
        this.reportRepository = reportRepository;
        this.authRepository = authRepository;
        this.appConfigRepository = appConfigRepository;
        loadReportReasons();
    }

    private void loadReportReasons() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                _appConfig.postValue(config);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to load report reasons."));
            }
        });
    }

    public void submitReport(String contentId, String contentType, String reportedUserId, String reasonId, String details) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _toastMessage.setValue(new Event<>("You must be logged in to report."));
            return;
        }

        _isLoading.setValue(true);

        Report report = new Report();
        report.setReportingUserId(currentUser.getUid());
        report.setReportedContentId(contentId);
        report.setReportedContentType(contentType);
        report.setReportedUserId(reportedUserId); // ID của chủ sở hữu nội dung
        report.setReason(reasonId); // ID của lý do
        report.setDetails(details);
        report.setStatus("pending_review");

        reportRepository.submitReport(report, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.postValue(false);
                _submitSuccess.postValue(new Event<>(true));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Failed to submit report: " + e.getMessage()));
            }
        });
    }
}