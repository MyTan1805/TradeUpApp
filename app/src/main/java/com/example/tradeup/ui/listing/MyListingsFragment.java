package com.example.tradeup.ui.listing;// package: com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentMyListingsBinding; // Import ViewBinding
import com.example.tradeup.ui.adapters.MyListingsPagerAdapter; // Import PagerAdapter
import com.example.tradeup.ui.listing.MyListingsViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyListingsFragment extends Fragment {

    // Không cần các hằng số ARG_PARAM nữa

    private FragmentMyListingsBinding binding;
    private MyListingsViewModel viewModel;
    private MyListingsPagerAdapter pagerAdapter;

    /**
     * Constructor rỗng là bắt buộc, không cần sửa đổi.
     */
    public MyListingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel ở đây, trong onCreate() để nó tồn tại qua các lần tạo lại View
        viewModel = new ViewModelProvider(this).get(MyListingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng ViewBinding để inflate layout một cách an toàn và hiệu quả
        binding = FragmentMyListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập ViewPager và TabLayout
        setupViewPagerAndTabs();

        // Gán sự kiện click cho nút FAB
        binding.fabAddNew.setOnClickListener(v -> {
            // Điều hướng đến màn hình AddItem
            NavHostFragment.findNavController(this).navigate(R.id.addItemFragment);
        });
    }

    /**
     * Hàm này chịu trách nhiệm kết nối ViewPager2 và TabLayout với nhau.
     */
    private void setupViewPagerAndTabs() {
        // Tạo adapter cho ViewPager2
        pagerAdapter = new MyListingsPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        // Dùng TabLayoutMediator để "dịch" vị trí tab thành tên tab
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Active");
                    break;
                case 1:
                    tab.setText("Sold");
                    break;
                case 2:
                    tab.setText("Paused");
                    break;
            }
        }).attach(); // Đừng quên gọi attach()!
    }

    /**
     * Rất quan trọng: Giải phóng binding trong onDestroyView để tránh memory leak.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}