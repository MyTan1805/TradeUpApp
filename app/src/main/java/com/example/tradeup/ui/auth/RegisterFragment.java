package com.example.tradeup.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewKt;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.databinding.RegisterFragmentBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private RegisterFragmentBinding binding;
    private AuthViewModel authViewModel;

    private static final String TAG = "RegisterFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        Log.d(TAG, "onCreate called");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        binding = RegisterFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        setupObservers();
        setupClickListeners();
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners called");
        binding.btnDangKy.setOnClickListener(v -> {
            Log.d(TAG, "BTN_DANG_KY CLICKED!");
            String displayName = binding.txtTenDN.getText() != null ? binding.txtTenDN.getText().toString().trim() : "";
            String email = binding.txtEmail.getText() != null ? binding.txtEmail.getText().toString().trim() : "";
            String phoneNumber = binding.txtSoDT.getText() != null ? binding.txtSoDT.getText().toString().trim() : "";
            String password = binding.txtMatKhau.getText() != null ? binding.txtMatKhau.getText().toString() : "";
            String confirmPassword = binding.txtNhapLaiMatKhau.getText() != null ? binding.txtNhapLaiMatKhau.getText().toString() : "";

            if (validateInput(displayName, email, phoneNumber, password, confirmPassword)) {
                authViewModel.registerUser(email, password, displayName, phoneNumber);
            }
        });

        binding.txtDangNhap.setOnClickListener(v -> {
            Log.d(TAG, "txtDangNhap clicked");
            if (isAdded()) {
                try {
                    NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to Login FAILED", e);
                    Toast.makeText(requireContext(), "Lỗi điều hướng.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnGoogleRegister.setOnClickListener(v -> {
            Log.d(TAG, "btnGoogleRegister clicked");
            Toast.makeText(requireContext(), "Chức năng đăng ký bằng Google sắp ra mắt!", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput(
            String displayName,
            String email,
            String phoneNumber,
            String pass,
            String confirmPass
    ) {
        boolean isValid = true;

        if (displayName.isEmpty()) {
            binding.tilTenDN.setError(getString(R.string.error_ten_hien_thi_trong));
            isValid = false;
        } else {
            binding.tilTenDN.setError(null);
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_trong));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_khong_hop_le));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        if (phoneNumber.isEmpty()) {
            binding.tilSoDT.setError(getString(R.string.error_sdt_trong));
            isValid = false;
        } else if (phoneNumber.length() < 10) {
            binding.tilSoDT.setError(getString(R.string.error_sdt_khong_hop_le));
            isValid = false;
        } else {
            binding.tilSoDT.setError(null);
        }

        if (pass.isEmpty()) {
            binding.tilMatKhau.setError(getString(R.string.error_mat_khau_trong));
            isValid = false;
        } else if (pass.length() < 6) {
            binding.tilMatKhau.setError(getString(R.string.error_mat_khau_qua_ngan));
            isValid = false;
        } else {
            binding.tilMatKhau.setError(null);
        }

        if (confirmPass.isEmpty()) {
            binding.tilNhapLaiMatKhau.setError(getString(R.string.error_nhap_lai_mat_khau_trong));
            isValid = false;
        } else if (!pass.equals(confirmPass)) {
            binding.tilNhapLaiMatKhau.setError(getString(R.string.error_mat_khau_khong_khop));
            isValid = false;
        } else {
            binding.tilNhapLaiMatKhau.setError(null);
        }

        return isValid;
    }

    private void setupObservers() {
        Log.d(TAG, "setupObservers called");
        // Sử dụng getter để lấy LiveData
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            // Log tên lớp của state để debug
            Log.d(TAG, "AuthState observed: " + state.getClass().getSimpleName());

            if (state instanceof AuthState.Loading) {
                binding.progressBarRegister.setVisibility(View.VISIBLE);
            } else {
                binding.progressBarRegister.setVisibility(View.GONE);
            }
            binding.btnDangKy.setEnabled(!(state instanceof AuthState.Loading));

            if (state instanceof AuthState.Success) {
                AuthState.Success successState = (AuthState.Success) state; // Ép kiểu
                String message = successState.message != null ? successState.message : "Đăng ký thành công!";
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                Log.d(TAG, "AuthState.Success: " + message);
                if (isAdded()) {
                    try {
                        NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation after registration FAILED", e);
                    }
                }
                authViewModel.resetAuthStateToIdle();
            } else if (state instanceof AuthState.Error) {
                AuthState.Error errorState = (AuthState.Error) state; // Ép kiểu
                Log.e(TAG, "AuthState.Error: " + errorState.message);
                Toast.makeText(requireContext(), "Lỗi: " + errorState.message, Toast.LENGTH_LONG).show();
            } else if (state instanceof AuthState.Idle) {
                Log.d(TAG, "AuthState.Idle");
            } else if (state instanceof AuthState.Loading) {
                Log.d(TAG, "AuthState.Loading");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        // Sử dụng getter để lấy giá trị LiveData
        AuthState currentState = authViewModel.getAuthState().getValue();
        if (currentState instanceof AuthState.Success || currentState instanceof AuthState.Error) {
            authViewModel.resetAuthStateToIdle();
        }
        binding = null;
    }
}