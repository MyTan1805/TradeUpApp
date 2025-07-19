// File: src/main/java/com/example/tradeup/ui/auth/AuthViewModel.java
package com.example.tradeup.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Event;
import com.example.tradeup.core.utils.SessionManager;
import com.example.tradeup.core.utils.UserRoleManager; // *** BƯỚC 1: THÊM IMPORT ***
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final UserRoleManager userRoleManager; // *** BƯỚC 2: KHAI BÁO BIẾN ***

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<FirebaseUser>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<FirebaseUser>> getNavigationEvent() { return _navigationEvent; }

    @Inject
    public AuthViewModel(AuthRepository authRepository, UserRepository userRepository, SessionManager sessionManager, UserRoleManager userRoleManager) { // *** BƯỚC 3: INJECT VÀO CONSTRUCTOR ***
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.userRoleManager = userRoleManager; // *** BƯỚC 4: KHỞI TẠO ***
    }

    // *** BƯỚC 5: TẠO HÀM XỬ LÝ CHUNG SAU KHI ĐĂNG NHẬP THÀNH CÔNG ***
    private void handleSuccessfulLogin(FirebaseUser firebaseUser, String email, boolean rememberMe) {
        userRepository.getUserProfile(firebaseUser.getUid())
                .whenComplete((user, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null || user == null) {
                        _toastMessage.postValue(new Event<>("Could not retrieve user profile. Please try again."));
                        authRepository.logoutUser();
                        return;
                    }

                    // *** BƯỚC 6: KIỂM TRA VAI TRÒ VÀ CẬP NHẬT USERROLEMANAGER ***
                    if ("admin".equalsIgnoreCase(user.getRole())) {
                        userRoleManager.setAdmin(true);
                        _toastMessage.postValue(new Event<>("Welcome Admin!"));
                    } else {
                        userRoleManager.setAdmin(false);
                    }

                    sessionManager.setRememberMe(rememberMe);
                    sessionManager.saveEmail(rememberMe ? email : null);
                    _navigationEvent.postValue(new Event<>(firebaseUser));
                });
    }

    public void loginUser(String email, String password, boolean rememberMe) {
        _isLoading.setValue(true);
        authRepository.loginUser(email, password)
                .whenComplete((firebaseUser, throwable) -> {
                    if (throwable != null) {
                        _isLoading.postValue(false);
                        _toastMessage.postValue(new Event<>(throwable.getMessage()));
                    } else {
                        if (firebaseUser.isEmailVerified()) {
                            // Gọi hàm xử lý chung
                            handleSuccessfulLogin(firebaseUser, email, rememberMe);
                        } else {
                            _isLoading.postValue(false);
                            _toastMessage.postValue(new Event<>("Vui lòng xác thực email. Email mới đã được gửi."));
                            authRepository.sendEmailVerification(firebaseUser);
                            authRepository.logoutUser();
                        }
                    }
                });
    }

    public void loginWithGoogle(String idToken) {
        _isLoading.setValue(true);
        authRepository.loginWithGoogle(idToken)
                .thenCompose(firebaseUser ->
                        userRepository.getUserProfile(firebaseUser.getUid())
                                .thenCompose(userProfile -> {
                                    if (userProfile != null) {
                                        return CompletableFuture.completedFuture(firebaseUser);
                                    } else {
                                        User newUser = new User();
                                        newUser.setUid(firebaseUser.getUid());
                                        newUser.setEmail(firebaseUser.getEmail());
                                        newUser.setDisplayName(firebaseUser.getDisplayName());
                                        return userRepository.createUserProfile(newUser).thenApply(aVoid -> firebaseUser);
                                    }
                                })
                )
                .whenComplete((firebaseUser, throwable) -> {
                    if (throwable != null) {
                        _isLoading.postValue(false);
                        _toastMessage.postValue(new Event<>(throwable.getMessage()));
                    } else {
                        // Gọi hàm xử lý chung
                        handleSuccessfulLogin(firebaseUser, firebaseUser.getEmail(), true);
                    }
                });
    }

    // Các hàm registerUser và sendPasswordResetEmail không thay đổi, giữ nguyên
    public void registerUser(String email, String password, String displayName) {
        _isLoading.setValue(true);
        authRepository.registerUser(email, password)
                .thenCompose(firebaseUser -> {
                    User newUser = new User();
                    newUser.setUid(firebaseUser.getUid());
                    newUser.setEmail(firebaseUser.getEmail());
                    newUser.setDisplayName(displayName);
                    return userRepository.createUserProfile(newUser).thenApply(aVoid -> firebaseUser);
                })
                .thenCompose(authRepository::sendEmailVerification)
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>(throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản."));
                        authRepository.logoutUser();
                    }
                });
    }

    public void sendPasswordResetEmail(String email) {
        _isLoading.setValue(true);
        authRepository.sendPasswordResetEmail(email)
                .whenComplete((aVoid, throwable) -> {
                    _isLoading.postValue(false);
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>(throwable.getMessage()));
                    } else {
                        _toastMessage.postValue(new Event<>("Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư."));
                    }
                });
    }

    public String getRememberedEmail() {
        return sessionManager.shouldRememberMe() ? sessionManager.getEmail() : null;
    }
}