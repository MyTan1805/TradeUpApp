package com.example.tradeup.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event; // Sử dụng Event wrapper
import com.example.tradeup.core.utils.SessionManager; // Import SessionManager
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager; // << THÊM SessionManager

    // << SỬ DỤNG Event<String> cho các thông báo chỉ hiển thị một lần >>
    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() {
        return _toastMessage;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    // LiveData để báo hiệu điều hướng thành công
    private final MutableLiveData<Event<FirebaseUser>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<FirebaseUser>> getNavigationEvent() { return _navigationEvent; }


    @Inject
    public AuthViewModel(AuthRepository authRepository, UserRepository userRepository, SessionManager sessionManager) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager; // << Khởi tạo
    }

    // << FIX: Cải tiến logic đăng ký, làm phẳng "Callback Hell" >>
    public void registerUser(String email, String password, String displayName) {
        _isLoading.setValue(true);
        authRepository.registerUser(email, password, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Sau khi đăng ký Auth thành công, tạo profile
                checkAndCreateProfile(firebaseUser, displayName, false);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>(e.getMessage()));
            }
        });
    }

    private void createNewUserProfile(FirebaseUser firebaseUser, String displayName) {
        User newUser = new User();
        newUser.setUid(firebaseUser.getUid());
        newUser.setEmail(firebaseUser.getEmail());
        newUser.setDisplayName(displayName);
        // Khởi tạo các giá trị mặc định khác nếu cần

        userRepository.createUserProfile(newUser, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Sau khi tạo profile thành công, gửi email xác thực
                sendVerificationEmail(firebaseUser);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Tạo hồ sơ thất bại: " + e.getMessage()));
            }
        });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        authRepository.sendEmailVerification(user, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản."));
                // Logout để buộc người dùng phải đăng nhập lại sau khi xác thực
                authRepository.logoutUser();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Gửi email xác thực thất bại: " + e.getMessage()));
            }
        });
    }

    // << FIX: Cải tiến logic login >>
    public void loginUser(String email, String password, boolean rememberMe) {
        _isLoading.setValue(true);
        authRepository.loginUser(email, password, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                _isLoading.setValue(false);
                if (firebaseUser.isEmailVerified()) {
                    // Lưu trạng thái remember me
                    sessionManager.setRememberMe(rememberMe);
                    if (rememberMe) {
                        sessionManager.saveEmail(email);
                    } else {
                        sessionManager.saveEmail(null);
                    }
                    _isLoading.setValue(false);
                    _navigationEvent.setValue(new Event<>(firebaseUser));
                } else {
                    _isLoading.setValue(false);
                    _toastMessage.setValue(new Event<>("Vui lòng xác thực email. Email mới đã được gửi."));
                    authRepository.sendEmailVerification(firebaseUser, new Callback<Void>() {
                        @Override public void onSuccess(Void data) {}
                        @Override public void onFailure(@NonNull Exception e) {}
                    });
                    authRepository.logoutUser();
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>(e.getMessage()));
            }
        });
    }

    // << FIX: Cải tiến logic login Google >>
    public void loginWithGoogle(String idToken) {
        _isLoading.setValue(true);
        authRepository.loginWithGoogle(idToken, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Sau khi đăng nhập Auth thành công, kiểm tra hoặc tạo profile
                checkAndCreateProfile(firebaseUser, firebaseUser.getDisplayName(), true);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>(e.getMessage()));
            }
        });
    }

    private void checkAndCreateProfile(FirebaseUser firebaseUser, String displayName, boolean isSocialLogin) {
        userRepository.getUserProfile(firebaseUser.getUid(), new Callback<User>() {
            @Override
            public void onSuccess(User userProfile) {
                if (userProfile != null) {
                    // User đã tồn tại, cho đăng nhập ngay
                    _isLoading.setValue(false);
                    _navigationEvent.setValue(new Event<>(firebaseUser));
                } else {
                    // User mới, tạo profile mới
                    createNewUserProfile(firebaseUser, displayName, isSocialLogin);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Failed to check user profile: " + e.getMessage()));
            }
        });
    }

    private void createNewUserProfile(FirebaseUser firebaseUser, String displayName, boolean isSocialLogin) {
        User newUser = new User();
        newUser.setUid(firebaseUser.getUid());
        newUser.setEmail(firebaseUser.getEmail());
        newUser.setDisplayName(displayName);

        userRepository.createUserProfile(newUser, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Đối với login mạng xã hội, email đã được xác thực, cho vào luôn.
                // Đối với đăng ký thường, cần gửi mail.
                if (isSocialLogin) {
                    _isLoading.setValue(false);
                    _navigationEvent.setValue(new Event<>(firebaseUser));
                } else {
                    sendVerificationEmail(firebaseUser);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Failed to create profile: " + e.getMessage()));
            }
        });
    }

    public void sendPasswordResetEmail(String email) {
        _isLoading.setValue(true);
        authRepository.sendPasswordResetEmail(email, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư."));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>(e.getMessage()));
            }
        });
    }

    // Hàm để lấy thông tin cho checkbox "Remember me"
    public String getRememberedEmail() {
        if (sessionManager.shouldRememberMe()) {
            return sessionManager.getEmail();
        }
        return null;
    }
}