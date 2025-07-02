package com.example.tradeup.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentSettingsBinding;
import com.example.tradeup.ui.adapters.SettingsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements SettingsAdapter.OnSettingItemClickListener {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

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
        // << FIX: Không cần bind lại binding ở đây >>

        setupToolbar();
        setupRecyclerView();
        setupObservers();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    private void setupRecyclerView() {
        List<SettingItem> settingItems = createSettingItems();
        SettingsAdapter adapter = new SettingsAdapter(settingItems, this);
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
                // << FIX: SỬ DỤNG ĐÚNG ACTION ĐIỀU HƯỚNG VỀ LUỒNG AUTH >>
                // Action này đã được định nghĩa trong root_nav.xml
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_auth_nav);
            }
        });
    }

    // --- Xử lý sự kiện click từ Adapter ---
    @Override
    public void onNavigationItemClick(String tag) {
        // << FIX: Gọi đúng các hàm trong ViewModel >>
        switch (tag) {
            case "deactivate_account":
                showConfirmationDialog(
                        "Deactivate Account",
                        "Are you sure? You can reactivate your account by logging in again.",
                        "Deactivate",
                        () -> viewModel.deactivateAccount()
                );
                break;
            case "delete_account":
                showConfirmationDialog(
                        "Delete Account",
                        "This action is permanent and cannot be undone. You will need to re-enter your password to continue.",
                        "Continue",
                        () -> viewModel.onDeleteAccountClicked()
                );
                break;
            case "logout":
                showConfirmationDialog(
                        "Log Out",
                        "Are you sure you want to log out?",
                        "Log Out",
                        () -> viewModel.logout() // Gọi hàm logout của ViewModel khi người dùng nhấn OK
                );
                break;
            // TODO: Thêm các case cho các item khác như "Edit Profile", "Change Password"
            default:
                Toast.makeText(getContext(), "Clicked: " + tag, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onSwitchItemChanged(String tag, boolean isChecked) {
        Toast.makeText(getContext(), "Switch " + tag + " is now " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        // TODO: Lưu trạng thái mới này vào ViewModel hoặc SharedPreferences
    }

    // << THÊM LẠI CÁC HÀM TIỆN ÍCH >>
    private void showConfirmationDialog(String title, String message, String positiveButtonText, Runnable positiveAction) {
        if (getContext() == null) return;
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(positiveButtonText, (dialog, which) -> positiveAction.run())
                .show();
    }

    private void showPasswordReAuthDialog() {
        if (getContext() == null) return;
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reauthenticate, null);
        final TextInputEditText passwordInput = dialogView.findViewById(R.id.editTextPassword);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Please enter your password to confirm.")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
                    viewModel.confirmAndDeleteAccount(password);
                })
                .show();
    }

    // createSettingItems() giữ nguyên như cũ...
    private List<SettingItem> createSettingItems() {
        List<SettingItem> items = new ArrayList<>();

        // Nhóm 1: Profile
        items.add(new SettingItem.Navigation("edit_profile", R.drawable.ic_person, "Edit Profile"));
        items.add(new SettingItem.Navigation("change_password", R.drawable.ic_lock, "Change Password"));

        // Nhóm 2: Notifications
        items.add(new SettingItem.GroupHeader("NOTIFICATION PREFERENCES"));
        items.add(new SettingItem.Switch("switch_messages", "New Messages", true));
        items.add(new SettingItem.Switch("switch_offers", "Offers", true));
        items.add(new SettingItem.Switch("switch_updates", "App Updates", false));
        items.add(new SettingItem.Navigation("location_settings", R.drawable.ic_location_on, "Location Settings"));

        // Nhóm 3: Account Management
        items.add(new SettingItem.GroupHeader("ACCOUNT MANAGEMENT"));
        items.add(new SettingItem.Navigation("deactivate_account", 0, "Deactivate Account", R.color.status_warning, R.color.status_warning));
        items.add(new SettingItem.Navigation("delete_account", 0, "Delete Account", R.color.status_error, R.color.status_error));

        // Nhóm 4: Support
        items.add(new SettingItem.GroupHeader("SUPPORT"));
        items.add(new SettingItem.Navigation("help_support", R.drawable.ic_help_outline, "Help & Support"));
        // TODO: Lấy phiên bản ứng dụng động
        items.add(new SettingItem.Info(R.drawable.ic_info_outline, "About TradeUp", "v1.0.0"));

        // Nhóm 5: Logout
        items.add(new SettingItem.Logout());

        return items;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}