// File: src/main/java/com/example/tradeup/ui/admin/AdminDashboardPagerAdapter.java
package com.example.tradeup.ui.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tradeup.ui.admin.tabs.AdminItemsFragment;
import com.example.tradeup.ui.admin.tabs.AdminReportsFragment;
import com.example.tradeup.ui.admin.tabs.AdminUsersFragment;

public class AdminDashboardPagerAdapter extends FragmentStateAdapter {

    public AdminDashboardPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new AdminUsersFragment();
            case 2: return new AdminItemsFragment(); // *** THÊM CASE MỚI ***
            default: return new AdminReportsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // *** SỬA SỐ LƯỢNG TAB THÀNH 3 ***
    }
}