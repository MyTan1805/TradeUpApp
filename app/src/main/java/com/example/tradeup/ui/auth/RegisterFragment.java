package com.example.tradeup.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.databinding.RegisterFragmentBinding; // Sửa tên binding cho nhất quán

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    private RegisterFragmentBinding binding;
    private AuthViewModel viewModel; // Đổi tên authViewModel thành viewModel cho nhất quán

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        // << FIX: Xóa toàn bộ logic Google Sign-In không cần thiết ở đây >>
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = RegisterFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        // 1. Listener cho nút Đăng Ký
        binding.btnDangKy.setOnClickListener(v -> {
            String displayName = binding.txtTenDN.getText().toString().trim();
            String email = binding.txtEmail.getText().toString().trim();
            // String phoneNumber = binding.txtSoDT.getText().toString().trim(); // ViewModel mới không cần sđt
            String password = binding.txtMatKhau.getText().toString();
            String confirmPassword = binding.txtNhapLaiMatKhau.getText().toString();

            if (validateInput(displayName, email, password, confirmPassword)) {
                // Chỉ truyền những thông tin cần thiết
                viewModel.registerUser(email, password, displayName);
            }
        });

        // 2. Listener cho link "Đăng nhập"
        binding.layoutSignInLink.setOnClickListener(v -> { // << Sử dụng ID của LinearLayout cha
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

        // 3. Listener cho nút Đăng ký bằng Google (nếu bạn muốn giữ lại)
        binding.btnGoogleRegister.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Vui lòng đăng nhập bằng Google ở màn hình Đăng nhập.", Toast.LENGTH_LONG).show();
        });
    }

    private boolean validateInput(String displayName, String email, String pass, String confirmPass) {
        boolean isValid = true;
        // Logic validate của bạn đã rất tốt, giữ nguyên và chỉ sửa lỗi nhỏ
        if (displayName.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_display_name_empty));
            isValid = false;
        } else {
            binding.tilTenDN.setError(null);
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_empty));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        if (pass.isEmpty()) {
            binding.tilMatKhau.setError(getString(R.string.error_password_empty));
            isValid = false;
        } else if (pass.length() < 6) {
            binding.tilMatKhau.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            binding.tilMatKhau.setError(null);
        }

        if (confirmPass.isEmpty()) {
            binding.tilNhapLaiMatKhau.setError(getString(R.string.error_password_confirm_empty));
            isValid = false;
        } else if (!pass.equals(confirmPass)) {
            binding.tilNhapLaiMatKhau.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        } else {
            binding.tilNhapLaiMatKhau.setError(null);
        }

        // Không cần validate sđt ở đây nữa nếu nó không bắt buộc
        return isValid;
    }

    private void setupObservers() {
        // << FIX: Tách ra thành các observer riêng biệt >>
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnDangKy.setEnabled(!isLoading);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                // Nếu đăng ký thành công, quay lại màn hình Login
                if (message.contains("Đăng ký thành công")) {
                    if (isAdded()) {
                        NavHostFragment.findNavController(this).popBackStack();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}