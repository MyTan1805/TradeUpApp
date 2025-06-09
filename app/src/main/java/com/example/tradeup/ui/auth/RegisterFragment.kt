package com.example.tradeup.ui.auth // Đảm bảo package này đúng

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // Quan trọng để dùng isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tradeup.R
import com.example.tradeup.databinding.RegisterFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    // Khai báo biến binding, sử dụng đúng tên class Binding đã được tạo
    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        // Khởi tạo đối tượng binding
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root // Trả về root view từ binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners called")
        // Sử dụng binding để truy cập các View
        binding.btnDangKy.setOnClickListener {
            Log.d(TAG, "BTN_DANG_KY CLICKED!")// Thay vì binding.buttonRegister
            val displayName = binding.txtTenDN.text.toString().trim() // Sử dụng ID từ binding
            val email = binding.txtEmail.text.toString().trim()
            val phoneNumber = binding.txtSoDT.text.toString().trim() // Thêm nếu có
            val password = binding.txtMatKhau.text.toString()
            val confirmPassword = binding.txtNhapLaiMatKhau.text.toString()

            if (validateInput(displayName, email, phoneNumber, password, confirmPassword)) {
                // Gọi ViewModel với các tham số cần thiết
                // Nếu ViewModel của bạn không cần phoneNumber ở hàm registerUser, hãy bỏ nó đi
                authViewModel.registerUser(email, password, displayName,phoneNumber)
            }
        }

        binding.txtDangNhap.setOnClickListener { // Sử dụng ID từ binding cho TextView "Đăng nhập"
            if (isAdded) {
                try {

                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Lỗi điều hướng.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnGoogleRegister.setOnClickListener {
            // TODO: Triển khai logic đăng ký/đăng nhập bằng Google
            Toast.makeText(requireContext(), "Chức năng đăng ký bằng Google sắp ra mắt!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(
        displayName: String,
        email: String,
        phoneNumber: String, // Thêm nếu có
        pass: String,
        confirmPass: String
    ): Boolean {
        var isValid = true

        // Validate Display Name (Tên hiển thị)
        if (displayName.isEmpty()) {
            binding.tilTenDN.error = getString(R.string.error_ten_hien_thi_trong) // Sử dụng string resource
            isValid = false
        } else {
            binding.tilTenDN.error = null
        }

        // Validate Email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_trong)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_khong_hop_le)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validate Phone Number (Số điện thoại)
         if (phoneNumber.isEmpty()) { // Nếu bạn muốn nó là bắt buộc
            binding.tilSoDT.error = getString(R.string.error_sdt_trong)
            isValid = false
        } else if (phoneNumber.length < 10) { // Ví dụ validate đơn giản
            binding.tilSoDT.error = getString(R.string.error_sdt_khong_hop_le)
            isValid = false
        } // <<< THIẾU else ở đây
        else {
            binding.tilSoDT.error = null
        }
        // hoặc thêm logic validate phức tạp hơn cho số điện thoại Việt Nam
        if (phoneNumber.isEmpty()) { // Nếu bạn muốn nó là bắt buộc
            binding.tilSoDT.error = getString(R.string.error_sdt_trong)
            isValid = false
        } else if (phoneNumber.length < 10) { // Ví dụ validate đơn giản
            binding.tilSoDT.error = getString(R.string.error_sdt_khong_hop_le)
            isValid = false
        }
        else {
            binding.tilSoDT.error = null
        }


        // Validate Password
        if (pass.isEmpty()) {
            binding.tilMatKhau.error = getString(R.string.error_mat_khau_trong)
            isValid = false
        } else if (pass.length < 6) {
            binding.tilMatKhau.error = getString(R.string.error_mat_khau_qua_ngan)
            isValid = false
        } else {
            binding.tilMatKhau.error = null
        }

        // Validate Confirm Password
        if (confirmPass.isEmpty()) {
            binding.tilNhapLaiMatKhau.error = getString(R.string.error_nhap_lai_mat_khau_trong)
            isValid = false
        } else if (pass != confirmPass) {
            binding.tilNhapLaiMatKhau.error = getString(R.string.error_mat_khau_khong_khop)
            isValid = false
        } else {
            binding.tilNhapLaiMatKhau.error = null
        }

        return isValid
    }

    private fun setupObservers() {
        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBarRegister.isVisible = state is AuthState.Loading
            binding.btnDangKy.isEnabled = state !is AuthState.Loading // Disable nút khi đang loading

            when (state) {
                is AuthState.Success -> {
                    Toast.makeText(requireContext(), state.message ?: "Đăng ký thành công!", Toast.LENGTH_LONG).show()
                    if (isAdded) {
                        try {
                            // Điều hướng đến màn hình đăng nhập sau khi đăng ký thành công
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Lỗi điều hướng sau đăng ký.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    authViewModel.resetAuthStateToIdle()
                }
                is AuthState.Error -> {
                    Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                }
                AuthState.Idle -> {
                    // Trạng thái nghỉ, không cần làm gì thêm
                }
                AuthState.Loading -> {
                    // Đã xử lý isVisible và isEnabled ở trên
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.resetAuthStateToIdle() // Reset trạng thái ViewModel khi view bị hủy
        _binding = null // Quan trọng: giải phóng tham chiếu đến binding để tránh memory leaks
    }
}