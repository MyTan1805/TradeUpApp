package com.example.tradeup.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
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
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() {
        return _toastMessage;
    }

    // Event để báo cho UI biết khi nào cần điều hướng ra màn hình đăng nhập
    private final MutableLiveData<Event<Boolean>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getNavigationEvent() {
        return _navigationEvent;
    }

    // Event để yêu cầu UI hiển thị dialog nhập lại mật khẩu
    private final MutableLiveData<Event<Boolean>> _reAuthRequestEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getReAuthRequestEvent() { return _reAuthRequestEvent; }

    @Inject
    public SettingsViewModel(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void logout() {
        authRepository.logoutUser();
        _navigationEvent.setValue(new Event<>(true)); // Gửi tín hiệu điều hướng
    }

    public void deactivateAccount() {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("User not found. Please log in again."));
            return;
        }

        userRepository.deactivateUser(currentUser.getUid(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Account deactivated successfully."));
                logout(); // Tự động logout sau khi vô hiệu hóa
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Failed to deactivate account: " + e.getMessage()));
            }
        });
    }

    // Hàm này được gọi khi người dùng nhấn nút "Delete" lần đầu tiên
    public void onDeleteAccountClicked() {
        // Gửi tín hiệu yêu cầu Fragment hiển thị dialog nhập mật khẩu
        _reAuthRequestEvent.setValue(new Event<>(true));
    }

    // Hàm này được gọi sau khi người dùng nhập mật khẩu và nhấn OK
    public void confirmAndDeleteAccount(String password) {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("User not found. Please log in again."));
            return;
        }

        // Bước 1: Xóa tài khoản Authentication (đã bao gồm re-authentication)
        authRepository.reauthenticateAndDeleteCurrentUser(password, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Bước 2: Xóa tài liệu người dùng trên Firestore
                userRepository.deleteUser(currentUser.getUid(), new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        _isLoading.setValue(false);
                        _toastMessage.setValue(new Event<>("Account permanently deleted."));
                        _navigationEvent.setValue(new Event<>(true)); // Điều hướng về màn hình login
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        _isLoading.setValue(false);
                        // Lỗi hiếm gặp: Auth đã xóa nhưng Firestore thì không
                        _toastMessage.setValue(new Event<>("Auth account deleted, but failed to delete user data."));
                        _navigationEvent.setValue(new Event<>(true));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>(e.getMessage())); // Ví dụ: "Mật khẩu không đúng."
            }
        });
    }
}