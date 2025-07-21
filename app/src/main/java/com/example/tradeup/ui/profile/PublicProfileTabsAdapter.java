// File: src/main/java/com/example/tradeup/ui/profile/PublicProfileTabsAdapter.java

package com.example.tradeup.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tradeup.ui.profile.tabs.ProfileListingsFragment;
import com.example.tradeup.ui.profile.tabs.ProfileReviewsFragment;

/**
 * Adapter này quản lý các tab cho màn hình Public Profile.
 * Nó sẽ hiển thị các Fragment con: Listings và Reviews.
 * Các Fragment con này sẽ tự động lấy dữ liệu từ ProfileViewModel được chia sẻ.
 */
public class PublicProfileTabsAdapter extends FragmentStateAdapter {

    // Chúng ta có 2 tab
    private static final int NUM_TABS = 2;

    public PublicProfileTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về Fragment tương ứng với vị trí của tab
        switch (position) {
            case 1:
                // Tab thứ hai (index 1) là Reviews
                return new ProfileReviewsFragment();
            case 0:
            default:
                // Tab đầu tiên (index 0) và mặc định là Listings
                return new ProfileListingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        // Trả về tổng số tab
        return NUM_TABS;
    }
}