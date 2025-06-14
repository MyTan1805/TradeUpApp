package com.example.tradeup.ui.auth;

import androidx.annotation.NonNull; // Thêm import này
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.ContactInfo;
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

    private final MutableLiveData<AuthState> _authState = new MutableLiveData<>(new AuthState.Idle());
    // Getter public cho LiveData
    public LiveData<AuthState> getAuthState() {
        return _authState;
    }

    private final MutableLiveData<User> _userProfile = new MutableLiveData<>();
    public LiveData<User> getUserProfile() {
        return _userProfile;
    }

    private final MutableLiveData<Boolean> _logoutState = new MutableLiveData<>(false);
    public LiveData<Boolean> getLogoutState() {
        return _logoutState;
    }

    @Inject
    public AuthViewModel(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        checkCurrentUser();
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                fetchUserProfile(currentUser.getUid());
            } else {
                _authState.setValue(new AuthState.Error("Vui lòng xác thực email của bạn."));
            }
        }
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return authRepository.getCurrentUser();
    }

    public void registerUser(String email, String password, String displayName, String phoneNumber) {
        _authState.setValue(new AuthState.Loading());
        authRepository.registerUser(email, password, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                authRepository.sendEmailVerification(firebaseUser, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void resultVerification) {
                        ContactInfo contactInfo = new ContactInfo();
                        contactInfo.setPhone(phoneNumber); // Giả sử có setter

                        User newUserProfile = new User();
                        newUserProfile.setUid(firebaseUser.getUid());
                        newUserProfile.setDisplayName(displayName);
                        String userEmail = firebaseUser.getEmail();
                        newUserProfile.setEmail(userEmail != null ? userEmail : "");
                        newUserProfile.setContactInfo(contactInfo);

                        userRepository.createUserProfile(newUserProfile, new Callback<Void>() {
                            @Override
                            public void onSuccess(Void profileResult) {
                                _authState.postValue(new AuthState.Success(firebaseUser, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản."));
                            }

                            @Override
                            public void onFailure(Exception profileException) {
                                _authState.postValue(new AuthState.Error("Đăng ký Auth thành công nhưng tạo hồ sơ thất bại: " + profileException.getMessage()));
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Exception verificationEmailException) {
                        _authState.postValue(new AuthState.Error("Đăng ký thành công nhưng gửi email xác thực thất bại: " + verificationEmailException.getMessage()));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception authException) {
                String message = authException.getMessage() != null ? authException.getMessage() : "Lỗi đăng ký không xác định.";
                _authState.postValue(new AuthState.Error(message));
            }
        });
    }

    public void loginUser(String email, String password) {
        _authState.setValue(new AuthState.Loading());
        authRepository.loginUser(email, password, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                if (firebaseUser.isEmailVerified()) {
                    fetchUserProfile(firebaseUser.getUid());
                    _authState.postValue(new AuthState.Success(firebaseUser, "Đăng nhập thành công."));
                } else {
                    authRepository.sendEmailVerification(firebaseUser, new Callback<Void>() {
                        @Override public void onSuccess(Void result) {}
                        @Override public void onFailure(Exception e) {}
                    });
                    _authState.postValue(new AuthState.Error("Vui lòng xác thực email của bạn. Email xác thực đã được gửi lại."));
                    authRepository.logoutUser();
                }
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                String message = exception.getMessage() != null ? exception.getMessage() : "Email hoặc mật khẩu không đúng.";
                _authState.postValue(new AuthState.Error(message));
            }
        });
    }

    public void fetchUserProfile(String uid) {
        userRepository.getUserProfile(uid, new Callback<User>() {
            @Override
            public void onSuccess(User profile) {
                _userProfile.postValue(profile);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _userProfile.postValue(null);
            }
        });
    }

    public void sendPasswordResetEmail(String email) {
        _authState.setValue(new AuthState.Loading());
        authRepository.sendPasswordResetEmail(email, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                FirebaseUser currentUser = authRepository.getCurrentUser(); // Có thể null
                _authState.postValue(new AuthState.Success(currentUser, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn."));
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                String message = exception.getMessage() != null ? exception.getMessage() : "Lỗi gửi email đặt lại mật khẩu.";
                _authState.postValue(new AuthState.Error(message));
            }
        });
    }

    public void logoutUser() {
        authRepository.logoutUser();
        _userProfile.setValue(null);
        _authState.setValue(new AuthState.Idle());
        _logoutState.setValue(true);
    }

    public void onLogoutCompleted() {
        _logoutState.setValue(false);
    }

    public void resetAuthStateToIdle() {
        _authState.setValue(new AuthState.Idle());
    }
}