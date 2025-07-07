package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentMyListingsBinding;
import com.example.tradeup.ui.adapters.MyListingsPagerAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyListingsFragment extends Fragment {

    private FragmentMyListingsBinding binding;
    private MyListingsViewModel viewModel;
    private MyListingsPagerAdapter pagerAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyListingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupToolbar();
        setupViewPagerAndTabs();
        setupObservers();

        if (getArguments() != null) {
            int defaultTabIndex = getArguments().getInt("defaultTabIndex", 0);
            binding.viewPager.setCurrentItem(defaultTabIndex, false);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
    }

    private void setupViewPagerAndTabs() {
        pagerAdapter = new MyListingsPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

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
        }).attach();
    }

    private void setupObservers() {
        // << FIX: Lắng nghe LiveData state duy nhất >>
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            // Hiển thị/ẩn ProgressBar toàn màn hình
            binding.progressBarMyListings.setVisibility(state instanceof MyListingsState.Loading ? View.VISIBLE : View.GONE);

            if (state instanceof MyListingsState.Error) {
                // Hiển thị lỗi (có thể dùng Toast hoặc một UI lỗi riêng)
                Toast.makeText(getContext(), ((MyListingsState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe sự kiện điều hướng
        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            MyListingsNavigationEvent navEvent = event.getContentIfNotHandled();
            if (navEvent != null) {
                if (navEvent instanceof MyListingsNavigationEvent.ToEditItem) {
                    Bundle args = new Bundle();
                    args.putString("itemId", ((MyListingsNavigationEvent.ToEditItem) navEvent).itemId);
                    navController.navigate(R.id.action_global_to_editItemFragment, args);
                } else if (navEvent instanceof MyListingsNavigationEvent.ToRateBuyer) {
                    MyListingsNavigationEvent.ToRateBuyer rateArgs = (MyListingsNavigationEvent.ToRateBuyer) navEvent;
                    Bundle args = new Bundle();
                    args.putString("transactionId", rateArgs.transactionId);
                    args.putString("ratedUserId", rateArgs.ratedUserId);
                    args.putString("itemId", rateArgs.itemId);
                    navController.navigate(R.id.action_global_to_submitReviewFragment, args);
                }
            }
        });

        // Lắng nghe Toast message
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if(message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}