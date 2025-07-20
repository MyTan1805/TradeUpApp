// File: src/main/java/com/example/tradeup/ui/settings/SettingsFragment.java
package com.example.tradeup.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // *** THÊM IMPORT NÀY ***
import androidx.navigation.fragment.NavHostFragment; // *** THÊM IMPORT NÀY ***
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.core.utils.UserRoleManager;
import com.example.tradeup.databinding.FragmentSettingsBinding;
import com.example.tradeup.ui.adapters.SettingsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements SettingsAdapter.OnSettingItemClickListener {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private NavController navController; // *** KHAI BÁO BIẾN Ở ĐÂY ***

    @Inject
    UserRoleManager userRoleManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // *** KHỞI TẠO NAVCONTROLLER Ở ĐÂY ***
        navController = NavHostFragment.findNavController(this);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                // Sử dụng NavController đã khởi tạo
                navController.navigateUp();
            }
        });
    }

    private void setupRecyclerView() {
        SettingsAdapter adapter = new SettingsAdapter(createSettingItems(), this);
        binding.recyclerViewSettings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewSettings.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getReAuthRequestEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                showPasswordReAuthDialog();
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Hiện/ẩn một ProgressBar toàn màn hình
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            Boolean shouldNavigate = event.getContentIfNotHandled();
            if (shouldNavigate != null && shouldNavigate && isAdded()) {
                navController.navigate(R.id.action_global_to_auth_nav);
            }
        });
    }

    @Override
    public void onNavigationItemClick(String tag) {
        if (!isAdded()) return;

        switch (tag) {
            case "admin_dashboard":
                navController.navigate(R.id.action_settingsFragment_to_adminDashboardFragment);
                break;
            case "deactivate_account":
                showConfirmationDialog(
                        getString(R.string.dialog_confirm_deactivate_title), // SỬA Ở ĐÂY
                        getString(R.string.dialog_confirm_deactivate_message), // SỬA Ở ĐÂY
                        "Deactivate", // Có thể tạo string resource cho "Deactivate"
                        () -> viewModel.deactivateAccount()
                );
                break;
            case "delete_account":
                showConfirmationDialog(
                        getString(R.string.dialog_confirm_delete_title), // SỬA Ở ĐÂY
                        getString(R.string.dialog_confirm_delete_message), // SỬA Ở ĐÂY
                        "Continue", // Có thể tạo string resource cho "Continue"
                        () -> viewModel.onDeleteAccountClicked()
                );
                break;
            case "logout":
                showConfirmationDialog(
                        getString(R.string.dialog_confirm_logout_title), // SỬA Ở ĐÂY
                        getString(R.string.dialog_confirm_logout_message), // SỬA Ở ĐÂY
                        getString(R.string.settings_logout), // SỬA Ở ĐÂY
                        () -> viewModel.logout()
                );
                break;
            case "change_password":
                Toast.makeText(getContext(), "Change Password clicked", Toast.LENGTH_SHORT).show();
                break;
            case "edit_profile":
                navController.navigate(R.id.action_global_to_editProfileFragment);
                break;
            default:
                Log.d("SettingsFragment", "Unhandled tag: " + tag);
                break;
        }
    }


    @Override
    public void onSwitchItemChanged(String tag, boolean isChecked) {
        Toast.makeText(getContext(), "Switch " + tag + " is now " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
    }

    private void showConfirmationDialog(String title, String message, String positiveButtonText, Runnable positiveAction) {
        if (getContext() == null) return;
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.action_cancel), null) // SỬA Ở ĐÂY
                .setPositiveButton(positiveButtonText, (dialog, which) -> positiveAction.run())
                .show();
    }

    private void showPasswordReAuthDialog() {
        if (getContext() == null) return;
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reauthenticate, null);
        final TextInputEditText passwordInput = dialogView.findViewById(R.id.editTextPassword);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(getString(R.string.dialog_reauth_title)) // SỬA Ở ĐÂY
                .setMessage(getString(R.string.dialog_reauth_message)) // SỬA Ở ĐÂY
                .setView(dialogView)
                .setNegativeButton(getString(R.string.action_cancel), null) // SỬA Ở ĐÂY
                .setPositiveButton(getString(R.string.dialog_reauth_positive_button), (dialog, which) -> { // SỬA Ở ĐÂY
                    String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
                    viewModel.confirmAndDeleteAccount(password);
                })
                .show();
    }

    private List<SettingItem> createSettingItems() {
        List<SettingItem> items = new ArrayList<>();

        if (userRoleManager.isAdmin()) {
            items.add(new SettingItem.GroupHeader(getString(R.string.settings_header_admin)));
            items.add(new SettingItem.Navigation("admin_dashboard", R.drawable.ic_admin_panel, getString(R.string.settings_admin_dashboard)));
        }

        items.add(new SettingItem.GroupHeader(getString(R.string.settings_header_account)));
        items.add(new SettingItem.Navigation("edit_profile", R.drawable.ic_person, getString(R.string.settings_edit_profile)));
        items.add(new SettingItem.Navigation("change_password", R.drawable.ic_lock, getString(R.string.settings_change_password)));

        items.add(new SettingItem.GroupHeader(getString(R.string.settings_header_notifications)));
        items.add(new SettingItem.Switch("switch_messages", getString(R.string.settings_pref_messages), true));
        items.add(new SettingItem.Switch("switch_offers", getString(R.string.settings_pref_offers), true));

        items.add(new SettingItem.GroupHeader(getString(R.string.settings_header_management)));
        items.add(new SettingItem.Navigation(
                "deactivate_account",
                R.drawable.ic_block,
                getString(R.string.settings_deactivate_account),
                R.color.status_warning,
                R.color.status_warning
        ));
        items.add(new SettingItem.Navigation(
                "delete_account",
                R.drawable.ic_delete,
                getString(R.string.settings_delete_account),
                R.color.status_error,
                R.color.status_error
        ));

        items.add(new SettingItem.GroupHeader(getString(R.string.settings_header_support)));
        items.add(new SettingItem.Navigation("help_support", R.drawable.ic_help, getString(R.string.settings_help)));
        items.add(new SettingItem.Info(R.drawable.ic_info, getString(R.string.settings_about), getString(R.string.settings_app_version)));

        items.add(new SettingItem.Logout());

        return items;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}