// File: src/main/java/com/example/tradeup/ui/notifications/NotificationsViewModel.java
package com.example.tradeup.ui.notifications;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Notification;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotificationsViewModel extends ViewModel {
    private final NotificationRepository notificationRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Event<Notification>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<Notification>> getNavigationEvent() { return _navigationEvent; }
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<List<Notification>> _notifications = new MutableLiveData<>();
    public LiveData<List<Notification>> getNotifications() { return _notifications; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    @Inject
    public NotificationsViewModel(NotificationRepository notificationRepository, AuthRepository authRepository) {
        this.notificationRepository = notificationRepository;
        this.authRepository = authRepository;
        loadNotifications();
    }

    public void loadNotifications() {
        _isLoading.setValue(true);
        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) {
            _toastMessage.setValue(new Event<>("Please log in to see notifications."));
            _notifications.setValue(Collections.emptyList());
            _isLoading.setValue(false);
            return;
        }

        notificationRepository.getNotifications(user.getUid(), 50, new Callback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                _notifications.postValue(data);
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to load notifications: " + e.getMessage()));
                _isLoading.postValue(false);
            }
        });
    }
    public void onNotificationClicked(Notification notification) {
        // 1. Gửi sự kiện điều hướng ngay lập tức để UI phản hồi nhanh.
        _navigationEvent.setValue(new Event<>(notification));

        // 2. Nếu thông báo chưa đọc, gọi repository để cập nhật trạng thái trên backend.
        if (!notification.isRead()) {
            notificationRepository.markNotificationAsRead(notification.getId(), new Callback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    updateNotificationInLocalList(notification.getId());
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("NotificationsViewModel", "Failed to mark notification as read", e);
                }
            });
        }
    }

    private void updateNotificationInLocalList(String notificationId) {
        List<Notification> currentList = _notifications.getValue();
        if (currentList == null) return;

        List<Notification> newList = new ArrayList<>(currentList);
        for (int i = 0; i < newList.size(); i++) {
            Notification n = newList.get(i);
            if (n.getId().equals(notificationId)) {
                n.setRead(true); // Cập nhật trạng thái
                newList.set(i, n);
                _notifications.setValue(newList); // Đẩy danh sách mới lên LiveData
                break;
            }
        }
    }
}