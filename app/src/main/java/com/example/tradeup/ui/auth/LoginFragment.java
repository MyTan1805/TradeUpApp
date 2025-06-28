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
import com.example.tradeup.core.utils.SessionManager;
import com.example.tradeup.databinding.FragmentLoginBinding;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    @Inject
    SessionManager sessionManager;

    private static final String TAG = "LoginFragment";

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
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView called");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        loadRememberedCredentials();
        setupClickListeners();
        setupObservers();
    }

    private void loadRememberedCredentials() {
        if (sessionManager != null) {
            if (sessionManager.shouldRememberMe()) {
                if (binding.editTextEmailLogin.getText() != null) {
                    binding.editTextEmailLogin.setText(sessionManager.getEmail());
                }
                binding.checkboxRememberMe.setChecked(true);
                Log.d(TAG, "Loaded remembered email: " + sessionManager.getEmail());
            } else {
                binding.checkboxRememberMe.setChecked(false);
                Log.d(TAG, "Remember me was false.");
            }
        } else {
            Log.e(TAG, "SessionManager is null in loadRememberedCredentials.");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners called");

        binding.buttonLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            String email = binding.editTextEmailLogin.getText() != null ? binding.editTextEmailLogin.getText().toString().trim() : "";
            String password = binding.editTextPasswordLogin.getText() != null ? binding.editTextPasswordLogin.getText().toString() : "";
            boolean rememberMe = binding.checkboxRememberMe.isChecked();

            if (validateInput(email, password)) {
                Log.d(TAG, "Input validated. Calling ViewModel loginUser.");
                if (sessionManager != null) {
                    sessionManager.setRememberMe(rememberMe);
                    if (rememberMe) {
                        sessionManager.saveEmail(email);
                        Log.d(TAG, "Saved email for remember me: " + email);
                    } else {
                        sessionManager.clearEmail();
                        Log.d(TAG, "Cleared email for remember me.");
                    }
                } else {
                    Log.e(TAG, "SessionManager is null in login button click. Cannot save preferences.");
                }
                authViewModel.loginUser(email, password);
            } else {
                Log.d(TAG, "Input validation FAILED.");
            }
        });

        binding.textViewForgotPassword.setOnClickListener(v -> {
            Log.d(TAG, "Forgot Password link clicked");
            if (isAdded()) {
                try {
                    NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to ForgotPassword FAILED", e);
                }
            }
        });

        binding.textViewRegisterLink.setOnClickListener(v -> {
            Log.d(TAG, "Register link clicked");
            if (isAdded()) {
                try {
                    NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to Register FAILED", e);
                }
            }
        });

        binding.buttonGoogleLogin.setOnClickListener(v -> {
            Log.d(TAG, "Google Login button clicked");
            Toast.makeText(requireContext(), "Chức năng đăng nhập bằng Google sắp ra mắt!", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput(String email, String pass) {
        boolean isValid = true;
        if (email.isEmpty()) {
            binding.tilEmailLogin.setError(getString(R.string.error_email_trong));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailLogin.setError(getString(R.string.error_email_khong_hop_le));
            isValid = false;
        } else {
            binding.tilEmailLogin.setError(null);
        }

        if (pass.isEmpty()) {
            binding.tilPasswordLogin.setError(getString(R.string.error_mat_khau_trong));
            isValid = false;
        } else {
            binding.tilPasswordLogin.setError(null);
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
                binding.progressBarLogin.setVisibility(View.VISIBLE);
            } else {
                binding.progressBarLogin.setVisibility(View.GONE);
            }
            binding.buttonLogin.setEnabled(!(state instanceof AuthState.Loading));

            if (state instanceof AuthState.Success) {
                AuthState.Success successState = (AuthState.Success) state;
                String message = successState.message != null ? successState.message : "Đăng nhập thành công!";
                Log.d(TAG, "AuthState.Success: " + message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                if (isAdded()) {
                    try {
                        Log.d(TAG, "Navigating to Home/Main screen.");
                        NavHostFragment.findNavController(this).navigate(R.id.action_global_to_main_nav);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation to Home FAILED", e);
                    }
                }
                authViewModel.resetAuthStateToIdle();
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

        // Sử dụng getter để lấy LiveData
        authViewModel.getLogoutState().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut != null && isLoggedOut) {
                Log.d(TAG, "Logout state observed as true.");
                authViewModel.onLogoutCompleted();
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