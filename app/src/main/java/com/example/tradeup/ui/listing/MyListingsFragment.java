// File: src/main/java/com/example/tradeup/ui/listing/MyListingsFragment.java

package com.example.tradeup.ui.listing;

import android.app.AlertDialog;
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
    private MyListingsViewModel viewModel; // <<< SỬA 1: DÙNG ĐÚNG KIỂU VIEWMODEL
    private MyListingsPagerAdapter pagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // <<< SỬA 2: KHỞI TẠO ĐÚNG VIEWMODEL >>>
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
        setupViewPagerAndTabs();
        setupClickListeners();
        observeViewModel();
    }


    private void setupClickListeners() {
        binding.fabAddNew.setOnClickListener(v -> {
            if (isAdded()) {
                // Sửa lại để dùng action đã tạo
                NavHostFragment.findNavController(this).navigate(R.id.action_myListingsFragment_to_addItemFragment);
            }
        });
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBarMyListings.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // *** THÊM OBSERVER MỚI NÀY ***
        viewModel.getNavigateToEditEvent().observe(getViewLifecycleOwner(), event -> {
            Item itemToEdit = event.getContentIfNotHandled();
            if (itemToEdit != null) {
                Bundle args = new Bundle();
                args.putString("itemId", itemToEdit.getItemId());
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_editItemFragment, args);
            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}