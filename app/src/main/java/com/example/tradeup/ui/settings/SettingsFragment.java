package com.example.tradeup.ui.settings;// package: com.example.tradeup.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentSettingsBinding; // Dùng ViewBinding
import com.example.tradeup.ui.adapters.SettingsAdapter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements SettingsAdapter.OnSettingItemClickListener {

    private FragmentSettingsBinding binding;
    // TODO: Tạo SettingsViewModel để quản lý logic và trạng thái

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private void setupRecyclerView() {
        List<SettingItem> settingItems = createSettingItems();
        SettingsAdapter adapter = new SettingsAdapter(settingItems, this);
        binding.recyclerViewSettings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewSettings.setAdapter(adapter);
    }

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

    // --- Xử lý sự kiện click từ Adapter ---
    @Override
    public void onNavigationItemClick(String tag) {
        Toast.makeText(getContext(), "Clicked: " + tag, Toast.LENGTH_SHORT).show();
        // TODO: Dùng NavController để điều hướng dựa trên tag
        // switch (tag) {
        //    case "edit_profile":
        //        navController.navigate(...)
        //        break;
        // }
    }

    @Override
    public void onSwitchItemChanged(String tag, boolean isChecked) {
        Toast.makeText(getContext(), "Switch " + tag + " is now " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        // TODO: Lưu trạng thái mới này vào SharedPreferences hoặc ViewModel
    }
}