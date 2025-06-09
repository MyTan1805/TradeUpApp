package com.example.tradeup.ui.auth
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
import com.example.tradeup.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    companion object {
        private const val TAG = "ForgotPasswordFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        setupToolbar()
        setupClickListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbarForgotPassword.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation icon clicked (Back)")
            if (isAdded) {
                findNavController().popBackStack()
            }
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")

        binding.buttonResetPassword.setOnClickListener {
            Log.d(TAG, "Reset Password button clicked")
            val email = binding.editTextEmailForgotPassword.text.toString().trim()

            if (validateEmail(email)) {
                Log.d(TAG, "Email validated. Calling ViewModel sendPasswordResetEmail.")
                authViewModel.sendPasswordResetEmail(email)
            } else {
                Log.d(TAG, "Email validation FAILED.")
            }
        }

        binding.textViewBackToLogin.setOnClickListener {
            Log.d(TAG, "Back to Login link clicked")
            if (isAdded) {
                try {
                    // Giả sử bạn muốn quay lại màn hình Login.
                    // Nếu LoginFragment là màn hình trước đó trong backstack, popBackStack là đủ.
                    // Hoặc bạn có thể navigate đến một action cụ thể nếu cần.
                    findNavController().popBackStack(R.id.loginFragment, false)
                    // Nếu LoginFragment không phải lúc nào cũng ở ngay trước đó,
                    // bạn có thể cần một action cụ thể từ nav_graph:
                    // findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation to Login FAILED", e)
                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.tilEmailForgotPassword.error = getString(R.string.error_email_trong)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailForgotPassword.error = getString(R.string.error_email_khong_hop_le)
            isValid = false
        } else {
            binding.tilEmailForgotPassword.error = null
        }
        return isValid
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers called")
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "AuthState observed: $state")
            binding.progressBarForgotPassword.isVisible = state is AuthState.Loading
            binding.buttonResetPassword.isEnabled = state !is AuthState.Loading

            when (state) {
                is AuthState.Success -> {
                    // ViewModel đang trả về Success cho cả thành công gửi email reset
                    // Thông báo đã được set trong ViewModel
                    Log.d(TAG, "AuthState.Success (Password Reset Email Sent): ${state.message}")
                    Toast.makeText(requireContext(), state.message ?: "Email đặt lại mật khẩu đã được gửi.", Toast.LENGTH_LONG).show()
                    // Tùy chọn: Điều hướng người dùng quay lại màn hình đăng nhập hoặc hiển thị thông báo rõ ràng hơn
                    if (isAdded) {
                        // findNavController().popBackStack() // Quay lại màn hình trước đó
                    }
                    authViewModel.resetAuthStateToIdle() // Reset trạng thái sau khi xử lý
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