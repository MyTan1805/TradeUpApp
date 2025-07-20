package com.example.tradeup.ui.auth;

import android.os.Bundle;
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
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private AuthViewModel viewModel; // Đổi tên cho nhất quán

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        // 1. Nút Back trên Toolbar
        binding.toolbarForgotPassword.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                NavigationUI.navigateUp(NavHostFragment.findNavController(this), (AppBarConfiguration) null);
            }
        });

        // 2. Nút Gửi Email
        binding.buttonResetPassword.setOnClickListener(v -> {
            String email = binding.editTextEmailForgotPassword.getText().toString().trim();
            if (validateEmail(email)) {
                viewModel.sendPasswordResetEmail(email);
            }
        });

        // 3. Link Quay lại Đăng nhập
        binding.textViewBackToLogin.setOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            binding.tilEmailForgotPassword.setError(getString(R.string.error_email_empty));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailForgotPassword.setError(getString(R.string.error_email_invalid));
            return false;
        } else {
            binding.tilEmailForgotPassword.setError(null);
            return true;
        }
    }

    private void setupObservers() {
        // Lắng nghe trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarForgotPassword.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonResetPassword.setEnabled(!isLoading);
        });

        // Lắng nghe các thông báo Toast
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                // << CẢI TIẾN UX >>: Nếu gửi thành công, hiển thị màn hình success
                if (message.contains("Đã gửi email")) {
                    showSuccessState();
                }
            }
        });
    }

    // << THÊM HÀM NÀY: Cải thiện UX sau khi gửi thành công >>
    private void showSuccessState() {
        // Ẩn form nhập email và nút gửi
        binding.tilEmailForgotPassword.setVisibility(View.GONE);
        binding.buttonResetPassword.setVisibility(View.GONE);

        // Thay đổi nội dung của các text view để thông báo
        binding.textViewInstruction.setText(R.string.forgot_password_success_title);
        binding.textViewBackToLogin.setText(R.string.forgot_password_success_action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}