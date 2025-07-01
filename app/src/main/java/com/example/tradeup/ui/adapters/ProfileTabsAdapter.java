// File: src/main/java/com/example/tradeup/ui/adapters/ProfileTabsAdapter.java

package com.example.tradeup.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// *** THÊM CÁC DÒNG IMPORT CHÍNH XÁC NÀY ***
import com.example.tradeup.ui.profile.tabs.ProfileListingsFragment;
import com.example.tradeup.ui.profile.tabs.ProfileReviewsFragment;
import com.example.tradeup.ui.profile.tabs.ProfileSoldFragment;


public class ProfileTabsAdapter extends FragmentStateAdapter {

    private final String[] tabTitles = new String[]{"Listings", "Sold", "Reviews"};

    public ProfileTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new ProfileSoldFragment();
            case 2:
                return new ProfileReviewsFragment(); // Lỗi của bạn ở đây
            default: // case 0
                return new ProfileListingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }

    public String getTabTitle(int position) {
        return tabTitles[position];
    }
}