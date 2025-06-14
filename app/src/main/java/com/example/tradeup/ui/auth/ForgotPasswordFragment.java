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
import androidx.core.view.ViewKt; // Cho isVisible
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private AuthViewModel authViewModel;

    private static final String TAG = "ForgotPasswordFragment";

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
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView called");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        setupToolbar();
        setupClickListeners();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbarForgotPassword.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar navigation icon clicked (Back)");
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners called");

        binding.buttonResetPassword.setOnClickListener(v -> {
            Log.d(TAG, "Reset Password button clicked");
            String email = "";
            if (binding.editTextEmailForgotPassword.getText() != null) {
                email = binding.editTextEmailForgotPassword.getText().toString().trim();
            }

            if (validateEmail(email)) {
                Log.d(TAG, "Email validated. Calling ViewModel sendPasswordResetEmail.");
                authViewModel.sendPasswordResetEmail(email);
            } else {
                Log.d(TAG, "Email validation FAILED.");
            }
        });

        binding.textViewBackToLogin.setOnClickListener(v -> {
            Log.d(TAG, "Back to Login link clicked");
            if (isAdded()) {
                try {
                    // Giả sử LoginFragment là destination trước đó hoặc có action cụ thể
                    // NavHostFragment.findNavController(this).popBackStack(R.id.loginFragment, false);
                    // Hoặc nếu có action từ ForgotPassword về Login:
                    NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to Login FAILED", e);
                    Toast.makeText(requireContext(), "Lỗi điều hướng.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateEmail(String email) {
        boolean isValid = true;
        if (email.isEmpty()) {
            binding.tilEmailForgotPassword.setError(getString(R.string.error_email_trong));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailForgotPassword.setError(getString(R.string.error_email_khong_hop_le));
            isValid = false;
        } else {
            binding.tilEmailForgotPassword.setError(null);
        }
        return isValid;
    }

    private void setupObservers() {
        Log.d(TAG, "setupObservers called");
        // Sử dụng getter để lấy LiveData
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return; // Kiểm tra null
            Log.d(TAG, "AuthState observed: " + state.getClass().getSimpleName());

            if (state instanceof AuthState.Loading) {
                binding.progressBarForgotPassword.setVisibility(View.VISIBLE);
            } else {
                binding.progressBarForgotPassword.setVisibility(View.GONE);
            }
            binding.buttonResetPassword.setEnabled(!(state instanceof AuthState.Loading));

            if (state instanceof AuthState.Success) {
                AuthState.Success successState = (AuthState.Success) state;
                // ViewModel đã trả về Success cho cả thành công gửi email reset
                // Thông báo đã được set trong ViewModel (state.message)
                String message = successState.message != null ? successState.message : getString(R.string.email_dat_lai_da_duoc_gui);
                Log.d(TAG, "AuthState.Success (Password Reset Email Sent): " + message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                // Tùy chọn: Điều hướng người dùng quay lại màn hình đăng nhập hoặc hiển thị thông báo rõ ràng hơn
                if (isAdded()) {
                    // Ví dụ: tự động quay lại sau vài giây hoặc khi người dùng nhấn OK trên một dialog
                    // NavHostFragment.findNavController(this).popBackStack(R.id.loginFragment, false);
                }
                authViewModel.resetAuthStateToIdle(); // Reset trạng thái sau khi xử lý
            } else if (state instanceof AuthState.Error) {
                AuthState.Error errorState = (AuthState.Error) state;
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