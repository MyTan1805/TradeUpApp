package com.example.tradeup.ui.auth // Đảm bảo package này đúng

// QUAN TRỌNG: THAY THẾ ĐƯỜNG DẪN NÀY BẰNG ĐƯỜNG DẪN ĐÚNG ĐẾN FILE SessionManager.kt CỦA BẠN
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tradeup.R
import com.example.tradeup.core.utils.SessionManager
import com.example.tradeup.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    @Inject // Hilt sẽ inject SessionManager được cung cấp bởi AppModule
    lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "LoginFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        loadRememberedCredentials()
        setupClickListeners()
        setupObservers()
    }

    private fun loadRememberedCredentials() {
        // Kiểm tra xem sessionManager đã được inject chưa (để tránh NullPointerException nếu có vấn đề với Hilt)
        if (::sessionManager.isInitialized) {
            if (sessionManager.shouldRememberMe()) {
                binding.editTextEmailLogin.setText(sessionManager.getEmail())
                binding.checkboxRememberMe.isChecked = true
                Log.d(TAG, "Loaded remembered email: ${sessionManager.getEmail()}")
            } else {
                binding.checkboxRememberMe.isChecked = false
                Log.d(TAG, "Remember me was false.")
            }
        } else {
            Log.e(TAG, "SessionManager not initialized in loadRememberedCredentials. Hilt injection might have failed.")
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")

        binding.buttonLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            val email = binding.editTextEmailLogin.text.toString().trim()
            val password = binding.editTextPasswordLogin.text.toString()
            val rememberMe = binding.checkboxRememberMe.isChecked

            if (validateInput(email, password)) {
                Log.d(TAG, "Input validated. Calling ViewModel loginUser.")
                if (::sessionManager.isInitialized) {
                    sessionManager.setRememberMe(rememberMe)
                    if (rememberMe) {
                        sessionManager.saveEmail(email)
                        Log.d(TAG, "Saved email for remember me: $email")
                    } else {
                        sessionManager.clearEmail()
                        Log.d(TAG, "Cleared email for remember me.")
                    }
                } else {
                    Log.e(TAG, "SessionManager not initialized in login button click. Cannot save preferences.")
                }
                authViewModel.loginUser(email, password)
            } else {
                Log.d(TAG, "Input validation FAILED.")
            }
        }

        binding.textViewForgotPassword.setOnClickListener {
            Log.d(TAG, "Forgot Password link clicked")
            if (isAdded) {
                try {
                    // TODO: Thay thế bằng action ID đến màn hình Quên mật khẩu của bạn
                    findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation to ForgotPassword FAILED", e)
                }
            }
        }

        binding.textViewRegisterLink.setOnClickListener {
            Log.d(TAG, "Register link clicked")
            if (isAdded) {
                try {
                    findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation to Register FAILED", e)
                }
            }
        }

        binding.buttonGoogleLogin.setOnClickListener {
            Log.d(TAG, "Google Login button clicked")
            // TODO: Triển khai logic đăng nhập bằng Google
            Toast.makeText(requireContext(), "Chức năng đăng nhập bằng Google sắp ra mắt!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        var isValid = true
        // Validate Email
        if (email.isEmpty()) {
            binding.tilEmailLogin.error = getString(R.string.error_email_trong)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailLogin.error = getString(R.string.error_email_khong_hop_le)
            isValid = false
        } else {
            binding.tilEmailLogin.error = null
        }

        // Validate Password
        if (pass.isEmpty()) {
            binding.tilPasswordLogin.error = getString(R.string.error_mat_khau_trong)
            isValid = false
        } else {
            binding.tilPasswordLogin.error = null
        }
        return isValid
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers called")
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "AuthState observed: $state")
            binding.progressBarLogin.isVisible = state is AuthState.Loading
            binding.buttonLogin.isEnabled = state !is AuthState.Loading

            when (state) {
                is AuthState.Success -> {
                    Log.d(TAG, "AuthState.Success: ${state.message}")
                    Toast.makeText(requireContext(), state.message ?: "Đăng nhập thành công!", Toast.LENGTH_LONG).show()
                    if (isAdded) {
                        try {
                            Log.d(TAG, "Navigating to Home/Main screen.")
                            // TODO: THAY THẾ R.id.action_loginFragment_to_homeFragment BẰNG ACTION ĐÚNG CỦA BẠN
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment) // Ví dụ
                        } catch (e: Exception) {
                            Log.e(TAG, "Navigation to Home FAILED", e)
                            Toast.makeText(requireContext(), "Lỗi điều hướng sau đăng nhập.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    authViewModel.resetAuthStateToIdle()
                }
                is AuthState.Error -> {
                    Log.e(TAG, "AuthState.Error: ${state.message}")
                    Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                }
                AuthState.Idle -> {
                    Log.d(TAG, "AuthState.Idle")
                }
                AuthState.Loading -> {
                    Log.d(TAG, "AuthState.Loading")
                }
            }
        }

        authViewModel.logoutState.observe(viewLifecycleOwner) {isLoggedOut ->
            if(isLoggedOut) {
                Log.d(TAG, "Logout state observed as true.")
                // Xử lý nếu cần, ví dụ xóa thông tin remember me nếu logout từ nơi khác và quay lại đây
                if (::sessionManager.isInitialized) {
                    // sessionManager.setRememberMe(false) // Tùy chọn
                    // sessionManager.clearEmail() // Tùy chọn
                }
                authViewModel.onLogoutCompleted() // Reset lại cờ này trong ViewModel
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        if (authViewModel.authState.value is AuthState.Success || authViewModel.authState.value is AuthState.Error) {
            authViewModel.resetAuthStateToIdle()
        }
        _binding = null
    }
}