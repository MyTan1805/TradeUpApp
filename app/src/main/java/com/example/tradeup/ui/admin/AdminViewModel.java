// File: src/main/java/com/example/tradeup/ui/admin/AdminViewModel.java
package com.example.tradeup.ui.admin;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Rating;
import com.example.tradeup.data.model.Report;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.ReportReasonConfig;
import com.example.tradeup.data.repository.AdminRepository;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.RatingRepository;
import com.example.tradeup.data.repository.UserRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AdminViewModel extends ViewModel {
    private static final String TAG = "AdminViewModel";

    private final AdminRepository adminRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;
    private final RatingRepository ratingRepository;
    private final MutableLiveData<Boolean> _isItemSearchLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isItemSearchLoading() { return _isItemSearchLoading; }

    private final MutableLiveData<List<Item>> _itemSearchResults = new MutableLiveData<>();
    public LiveData<List<Item>> getItemSearchResults() { return _itemSearchResults; }

    private final MutableLiveData<Boolean> _isUserSearchLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isUserSearchLoading() { return _isUserSearchLoading; }

    private final MutableLiveData<List<User>> _userSearchResults = new MutableLiveData<>();
    public LiveData<List<User>> getUserSearchResults() { return _userSearchResults; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<List<Report>> _reports = new MutableLiveData<>();
    public LiveData<List<Report>> getReports() { return _reports; }

    private final MutableLiveData<Map<String, String>> _reasonMap = new MutableLiveData<>();
    public LiveData<Map<String, String>> getReasonMap() { return _reasonMap; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<Report>> _navigateToContentEvent = new MutableLiveData<>();
    public LiveData<Event<Report>> getNavigateToContentEvent() { return _navigateToContentEvent; }

    @Inject
    public AdminViewModel(AdminRepository adminRepository, ItemRepository itemRepository,
                          UserRepository userRepository, AppConfigRepository appConfigRepository,
                          RatingRepository ratingRepository) {
        this.adminRepository = adminRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
        this.ratingRepository = ratingRepository;

        loadInitialData();
    }

    private void loadInitialData() {
        loadReportReasons();
        loadPendingReports();
    }

    private void loadReportReasons() {
        appConfigRepository.getAppConfig(new com.example.tradeup.core.utils.Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig data) {
                if (data != null && data.getReportReasons() != null) {
                    Map<String, String> map = new HashMap<>();
                    for (ReportReasonConfig reason : data.getReportReasons()) {
                        map.put(reason.getId(), reason.getName());
                    }
                    _reasonMap.postValue(map);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to load report reasons."));
            }
        });
    }

    private void loadPendingReports() {
        _isLoading.setValue(true);
        adminRepository.getPendingReports(50)
                .whenComplete((reports, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Error loading reports: " + throwable.getMessage()));
                    } else {
                        _reports.postValue(reports);
                    }
                });
    }

    public void viewReportedContent(Report report) {
        _navigateToContentEvent.setValue(new Event<>(report));
    }

    public void deleteContent(Report report) {
        if (report == null) return;
        _isLoading.setValue(true);

        // *** BƯỚC 1: LẤY contentType MỘT LẦN DUY NHẤT ***
        String contentType = report.getReportedContentType();

        if ("listing".equalsIgnoreCase(contentType)) {
            // Logic cho item không đổi
            itemRepository.deleteItem(report.getReportedContentId(), new com.example.tradeup.core.utils.Callback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    resolveReport(report, "resolved_content_deleted");
                }
                @Override
                public void onFailure(@NonNull Exception e) {
                    _isLoading.postValue(false);
                    _toastMessage.postValue(new Event<>("Failed to delete item: " + e.getMessage()));
                }
            });
        } else if ("rating".equalsIgnoreCase(contentType)) {
            // *** BƯỚC 2: LOGIC XỬ LÝ RATING (ĐÃ ĐÚNG) SẼ NẰM Ở ĐÂY ***
            ratingRepository.getRatingById(report.getReportedContentId(), new Callback<Rating>() {
                @Override
                public void onSuccess(Rating rating) {
                    if (rating == null) {
                        _toastMessage.postValue(new Event<>("Review not found. It might have been already deleted."));
                        resolveReport(report, "resolved_content_not_found");
                        return;
                    }
                    adminRepository.deleteReviewAndRecalculateUserRating(
                            rating.getRatingId(),
                            rating.getRatedUserId(),
                            rating.getStars()
                    ).whenComplete((aVoid, throwable) -> {
                        if (throwable != null) {
                            _isLoading.postValue(false);
                            _toastMessage.postValue(new Event<>("Failed to delete review: " + throwable.getMessage()));
                        } else {
                            resolveReport(report, "resolved_content_deleted");
                        }
                    });
                }
                @Override
                public void onFailure(@NonNull Exception e) {
                    _isLoading.postValue(false);
                    _toastMessage.postValue(new Event<>("Failed to get review details: " + e.getMessage()));
                }
            });
        } else if ("profile".equalsIgnoreCase(contentType)) {
            // Logic cho profile không đổi
            _toastMessage.postValue(new Event<>("Profile deletion not implemented yet. Please suspend instead."));
            _isLoading.postValue(false);
        } else {
            // Thêm một trường hợp else để xử lý các contentType không xác định
            _toastMessage.postValue(new Event<>("Deletion for content type '" + contentType + "' is not supported."));
            _isLoading.postValue(false);
        }
    }

    public void suspendUserAccount(Report report) {
        if (report == null || report.getReportedUserId() == null) return;
        _isLoading.setValue(true);

        // *** SỬA LỖI Ở ĐÂY: Chuyển sang dùng CompletableFuture ***
        userRepository.deactivateUser(report.getReportedUserId())
                .thenCompose(aVoid -> {
                    // Sau khi tạm ngưng thành công, cập nhật trạng thái báo cáo
                    String adminNotes = "Action taken: resolved_user_suspended";
                    return adminRepository.updateReportStatus(report.getReportId(), "resolved_user_suspended", adminNotes);
                })
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to suspend user and resolve report: " + throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("User suspended and report resolved."));
                        loadPendingReports(); // Tải lại danh sách
                    }
                });
    }

    public void dismissReport(Report report) {
        resolveReport(report, "resolved_no_action");
    }

    private void resolveReport(Report report, String status) {
        _isLoading.setValue(true);
        String adminNotes = "Action taken: " + status;
        adminRepository.updateReportStatus(report.getReportId(), status, adminNotes)
                .whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        _isLoading.postValue(false);
                        _toastMessage.postValue(new Event<>("Failed to update report status: " + throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("Report resolved successfully."));
                        loadPendingReports(); // Tải lại danh sách để cập nhật UI
                    }
                });
    }

    public void searchUsers(String query) {
        if (query == null || query.trim().length() < 3) {
            _userSearchResults.setValue(Collections.emptyList()); // Xóa kết quả nếu query quá ngắn
            return;
        }
        _isUserSearchLoading.setValue(true);
        adminRepository.searchUsers(query.trim())
                .whenComplete((users, throwable) -> {
                    _isUserSearchLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("User search failed: " + throwable.getMessage()));
                    } else {
                        _userSearchResults.postValue(users);
                    }
                });
    }

    public void reactivateUser(String userId) {
        _isLoading.setValue(true);
        adminRepository.reactivateUser(userId)
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to reactivate user: " + throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("User reactivated successfully."));
                        // Tải lại danh sách user nếu cần
                    }
                });
    }

    public void changeUserRole(String userId, String newRole) {
        _isLoading.setValue(true);
        adminRepository.updateUserRole(userId, newRole)
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to change role: " + throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("User role updated to " + newRole));
                    }
                });
    }

    public void searchItems(String query) {
        if (query == null || query.trim().length() < 3) {
            _itemSearchResults.setValue(Collections.emptyList());
            return;
        }
        _isItemSearchLoading.setValue(true);
        adminRepository.searchAllItems(query.trim())
                .whenComplete((items, throwable) -> {
                    _isItemSearchLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Item search failed: " + throwable.getMessage()));
                    } else {
                        _itemSearchResults.postValue(items);
                    }
                });
    }
}