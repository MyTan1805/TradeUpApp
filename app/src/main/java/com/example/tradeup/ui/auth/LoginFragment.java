package com.example.tradeup.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.core.utils.FcmTokenUtil;
import com.example.tradeup.databinding.FragmentLoginBinding;
import com.example.tradeup.services.MyFirebaseMessagingService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        configureGoogleSignIn();
        setupGoogleSignInLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        setupUI();
        checkInitialInputState();
        setupObservers();
    }

    private void setupUI() {
        String rememberedEmail = viewModel.getRememberedEmail();
        if (rememberedEmail != null) {
            binding.editTextEmailLogin.setText(rememberedEmail);
            binding.checkboxRememberMe.setChecked(true);
        }
    }

    private void setupListeners() {
        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.editTextEmailLogin.getText().toString().trim();
            String password = binding.editTextPasswordLogin.getText().toString().trim();
            boolean rememberMe = binding.checkboxRememberMe.isChecked();
            viewModel.loginUser(email, password, rememberMe);
        });

        binding.buttonGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.textViewForgotPassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        );

        binding.layoutRegisterLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment)
        );

        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailInput = binding.editTextEmailLogin.getText().toString().trim();
                String passwordInput = binding.editTextPasswordLogin.getText().toString().trim();
                binding.buttonLogin.setEnabled(!emailInput.isEmpty() && !passwordInput.isEmpty());
            }
        };

        binding.editTextEmailLogin.addTextChangedListener(loginTextWatcher);
        binding.editTextPasswordLogin.addTextChangedListener(loginTextWatcher);
    }

    private void checkInputState() {
        String emailInput = binding.editTextEmailLogin.getText().toString().trim();
        String passwordInput = binding.editTextPasswordLogin.getText().toString().trim();
        binding.buttonLogin.setEnabled(!emailInput.isEmpty() && !passwordInput.isEmpty());
    }

    private void checkInitialInputState() {
        binding.buttonLogin.setEnabled(false);
        checkInputState();
    }

    private void setupObservers() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonLogin.setEnabled(!isLoading);
            binding.buttonGoogleLogin.setEnabled(!isLoading);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                // Sau khi đăng nhập thành công, lấy và lưu token FCM
                getAndSaveFcmToken();
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_main_nav);
            }
        });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        Log.w(TAG, "Google Sign-In cancelled by user.");
                    }
                });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                Log.d(TAG, "Google Sign-In successful, got ID Token.");
                viewModel.loginWithGoogle(account.getIdToken());
            } else {
                String errorMessage = "Google Sign-In failed: ID Token is null.";
                Log.e(TAG, errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            String errorMessage = "Google Sign-In failed with code: " + e.getStatusCode();
            Log.e(TAG, errorMessage, e);
            Toast.makeText(getContext(), "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức mới để lấy và lưu token FCM
    private void getAndSaveFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d(TAG, "FCM token retrieved: " + token);
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FcmTokenUtil.saveFcmTokenToFirestore(userId, token);
            } else {
                Log.w(TAG, "Failed to retrieve FCM token", task.getException());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to retrieve FCM token.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}