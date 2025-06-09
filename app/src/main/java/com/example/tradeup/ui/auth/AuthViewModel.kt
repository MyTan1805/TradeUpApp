package com.example.tradeup.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeup.data.model.ContactInfo
import com.example.tradeup.data.model.User
import com.example.tradeup.data.repository.AuthRepository
import com.example.tradeup.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class để quản lý các trạng thái của UI một cách rõ ràng hơn
sealed class AuthState {
    object Idle : AuthState() // Trạng thái ban đầu, không làm gì
    object Loading : AuthState() // Đang xử lý
    data class Success(val user: FirebaseUser, val message: String? = null) : AuthState() // Thành công, có thể kèm thông điệp
    data class Error(val message: String) : AuthState() // Lỗi
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository // Sẽ dùng để tạo/lấy profile user
) : ViewModel() {

    // LiveData cho trạng thái chung của các hành động auth (đăng ký, đăng nhập, quên mật khẩu)
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    // LiveData để giữ thông tin profile người dùng hiện tại từ Firestore
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    // LiveData cho trạng thái đăng xuất
    private val _logoutState = MutableLiveData<Boolean>()
    val logoutState: LiveData<Boolean> = _logoutState

    /**
     * Kiểm tra xem có người dùng nào đang đăng nhập không khi ViewModel được khởi tạo.
     * Nếu có, thử tải thông tin profile của họ.
     */
    init {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null && currentUser.isEmailVerified) { // Chỉ tải profile nếu đã xác thực email
            fetchUserProfile(currentUser.uid)
        } else if (currentUser != null && !currentUser.isEmailVerified) {
            // Có thể muốn gửi thông báo yêu cầu xác thực email ở đây
            // Hoặc để màn hình UI xử lý việc này dựa trên trạng thái của currentUser
            _authState.value = AuthState.Error("Vui lòng xác thực email của bạn.")
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun registerUser(email: String, pass: String, displayName: String, phoneNumber: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val registrationResult = authRepository.registerUser(email, pass)
            registrationResult.onSuccess { firebaseUser ->
                // Gửi email xác thực
                authRepository.sendEmailVerification(firebaseUser).onSuccess {
                    // Tạo profile người dùng trên Firestore
                    val newUserProfile = User(
                        uid = firebaseUser.uid,
                        displayName = displayName,
                        email = firebaseUser.email!!,
                        contactInfo = ContactInfo(phone = phoneNumber)
                        // Các trường khác sẽ có giá trị mặc định từ model User
                    )
                    val profileCreationResult = userRepository.createUserProfile(newUserProfile)
                    profileCreationResult.onSuccess {
                        _authState.value = AuthState.Success(firebaseUser, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.")
                    }.onFailure { profileException ->
                        // Lỗi khi tạo profile, đây là trường hợp khó xử lý
                        // Có thể cân nhắc xóa user trên Auth nếu tạo profile lỗi, hoặc cho phép user thử lại
                        _authState.value = AuthState.Error("Đăng ký Auth thành công nhưng tạo hồ sơ thất bại: ${profileException.message}")
                    }
                }.onFailure { verificationEmailException ->
                    // Lỗi khi gửi email xác thực
                    _authState.value = AuthState.Error("Đăng ký thành công nhưng gửi email xác thực thất bại: ${verificationEmailException.message}")
                }
            }.onFailure { authException ->
                _authState.value = AuthState.Error(authException.message ?: "Lỗi đăng ký không xác định.")
            }
        }
    }

    fun loginUser(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val loginResult = authRepository.loginUser(email, pass)
            loginResult.onSuccess { firebaseUser ->
                if (firebaseUser.isEmailVerified) {
                    fetchUserProfile(firebaseUser.uid) // Tải profile sau khi đăng nhập
                    _authState.value = AuthState.Success(firebaseUser, "Đăng nhập thành công.")
                } else {
                    // Yêu cầu người dùng xác thực email
                    authRepository.sendEmailVerification(firebaseUser) // Gửi lại email xác thực
                    _authState.value = AuthState.Error("Vui lòng xác thực email của bạn. Email xác thực đã được gửi lại.")
                    authRepository.logoutUser() // Đăng xuất người dùng nếu email chưa xác thực
                }
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Email hoặc mật khẩu không đúng.")
            }
        }
    }

    fun fetchUserProfile(uid: String) {
        // Không set Loading state ở đây vì đây là tác vụ nền sau khi đăng nhập/đăng ký
        viewModelScope.launch {
            userRepository.getUserProfile(uid).onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure {
                // Có thể log lỗi hoặc hiển thị thông báo kín đáo nếu cần
                _userProfile.value = null
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _authState.value = AuthState.Success(authRepository.getCurrentUser()!!, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn.") // Dùng Success để thông báo
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Lỗi gửi email đặt lại mật khẩu.")
            }
        }
    }

    fun logoutUser() {
        authRepository.logoutUser()
        _userProfile.value = null // Xóa profile đã tải
        _authState.value = AuthState.Idle // Reset trạng thái về ban đầu
        _logoutState.value = true // Thông báo đăng xuất thành công
    }

    fun onLogoutCompleted() {
        _logoutState.value = false
    }

    fun resetAuthStateToIdle() {
        _authState.value = AuthState.Idle
    }
}