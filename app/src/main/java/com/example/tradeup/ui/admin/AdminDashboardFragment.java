// File: src/main/java/com/example/tradeup/ui/admin/AdminDashboardFragment.java
package com.example.tradeup.ui.admin;

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
import com.example.tradeup.data.model.Report;
import com.example.tradeup.databinding.FragmentAdminDashboardBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminViewModel viewModel; // ViewModel này sẽ được chia sẻ cho các fragment con
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupToolbar();
        setupViewPagerAndTabs();
        observeViewModel(); // Chỉ observe các event toàn cục
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
    }

    private void setupViewPagerAndTabs() {
        AdminDashboardPagerAdapter adapter = new AdminDashboardPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 1:
                    tab.setText("Users");
                    break;
                case 2:
                    tab.setText("Items"); // *** THÊM TEXT CHO TAB MỚI ***
                    break;
                default:
                    tab.setText("Reports");
                    break;
            }
        }).attach();
    }

    // Fragment cha chỉ cần lắng nghe các sự kiện cần xử lý chung, như điều hướng
    private void observeViewModel() {
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigateToContentEvent().observe(getViewLifecycleOwner(), event -> {
            Report report = event.getContentIfNotHandled();
            if (report != null && isAdded()) {
                navigateToReportedContent(report);
            }
        });
    }

    private void navigateToReportedContent(Report report) {
        Bundle args = new Bundle();
        int destinationId = -1;

        if ("listing".equalsIgnoreCase(report.getReportedContentType())) {
            args.putString("itemId", report.getReportedContentId());
            destinationId = R.id.action_global_to_itemDetailFragment;
        } else if ("profile".equalsIgnoreCase(report.getReportedContentType())) {
            args.putString("userId", report.getReportedContentId());
            destinationId = R.id.action_global_to_publicProfileFragment;
        }

        if (destinationId != -1) {
            navController.navigate(destinationId, args);
        } else {
            Toast.makeText(getContext(), "Cannot navigate to content type: " + report.getReportedContentType(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}