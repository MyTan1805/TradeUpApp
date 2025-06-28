package com.example.tradeup.ui.adapters;

// package: com.example.tradeup.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.tradeup.ui.listing.ActiveListingsFragment;
import com.example.tradeup.ui.listing.PausedListingsFragment;
import com.example.tradeup.ui.listing.SoldListingsFragment;

public class MyListingsPagerAdapter extends FragmentStateAdapter {

    public MyListingsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ActiveListingsFragment();
            case 1:
                return new SoldListingsFragment();
            case 2:
                return new PausedListingsFragment();
            default:
                // Trả về một Fragment mặc định hoặc ném ra lỗi
                return new ActiveListingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Chúng ta có 3 tab
    }
}
