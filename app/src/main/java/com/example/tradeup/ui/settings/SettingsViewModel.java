// File: src/main/java/com/example/tradeup/ui/settings/SettingsViewModel.java
package com.example.tradeup.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<Boolean>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getNavigationEvent() { return _navigationEvent; }

    private final MutableLiveData<Event<Boolean>> _reAuthRequestEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getReAuthRequestEvent() { return _reAuthRequestEvent; }

    @Inject
    public SettingsViewModel(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void logout() {
        authRepository.logoutUser();
        _navigationEvent.setValue(new Event<>(true));
    }

    public void deactivateAccount() {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("User not found. Please log in again."));
            return;
        }

        userRepository.deactivateUser(currentUser.getUid())
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to deactivate account: " + throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("Account deactivated successfully."));
                        logout();
                    }
                });
    }

    public void onDeleteAccountClicked() {
        _reAuthRequestEvent.setValue(new Event<>(true));
    }

    public void confirmAndDeleteAccount(String password) {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("User not found. Please log in again."));
            return;
        }
        final String userId = currentUser.getUid();

        // Bước 1: Xóa tài khoản Authentication (đã bao gồm re-authentication)
        authRepository.reauthenticateAndDeleteCurrentUser(password)
                // Bước 2: Xâu chuỗi việc xóa tài liệu người dùng trên Firestore
                .thenCompose(aVoid -> userRepository.deleteUser(userId))
                // Bước 3: Xử lý kết quả cuối cùng
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>(throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("Account permanently deleted."));
                        _navigationEvent.postValue(new Event<>(true));
                    }
                });
    }
}