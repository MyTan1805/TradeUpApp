// File: src/main/java/com/example/tradeup/ui/profile/ProfileFragment.java
package com.example.tradeup.ui.profile;

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
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Rating;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentProfileBinding;
import com.example.tradeup.ui.adapters.ProfileTabsAdapter;
import com.example.tradeup.ui.report.ReportContentDialogFragment;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileTabsAdapter profileTabsAdapter;
    private NavController navController;

    // Không cần inject UserRoleManager ở đây nữa

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        setupViewPagerAndTabs();
        setupClickListeners();
        observeViewModel();

        // Không còn logic kiểm tra quyền admin ở đây
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi quay lại màn hình này để cập nhật thông tin mới nhất
        viewModel.loadAllData();
    }

    private void setupViewPagerAndTabs() {
        profileTabsAdapter = new ProfileTabsAdapter(this);
        binding.viewPagerProfileContent.setAdapter(profileTabsAdapter);

        new TabLayoutMediator(binding.tabLayoutProfile, binding.viewPagerProfileContent,
                (tab, position) -> tab.setText(profileTabsAdapter.getTabTitle(position))
        ).attach();
    }

    private void setupClickListeners() {
        binding.buttonEditProfile.setOnClickListener(v -> {
            navController.navigate(R.id.action_global_to_editProfileFragment);
        });

        binding.buttonSettings.setOnClickListener(v -> {
            navController.navigate(R.id.action_global_to_settingsFragment);
        });

        // Listener cho FAB đổi ảnh
        binding.fabChangeProfilePicture.setOnClickListener(v -> {
            navController.navigate(R.id.action_global_to_editProfileFragment);
        });

        binding.buttonMyOffers.setOnClickListener(v -> {
            if (isAdded()) {
                navController.navigate(R.id.action_profileFragment_to_offersFragment);
            }
        });

        binding.buttonMyTransactions.setOnClickListener(v -> {
            if (isAdded()) {
                navController.navigate(R.id.action_global_to_transactionHistoryFragment);
            }
        });

        binding.buttonSavedItems.setOnClickListener(v -> {
            if (isAdded()) {
                navController.navigate(R.id.action_global_to_savedItemsFragment);
            }
        });

        // Xóa listener cho nút admin dashboard

        // Các listener cho các ô thống kê
        View activeListingsStat = binding.layoutStats.getChildAt(0); // Vị trí 0 là Active Listings
        activeListingsStat.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("defaultTabIndex", 0); // 0 là tab "Active"
            navController.navigate(R.id.action_global_to_myListingsFragment, args);
        });

        View itemsSoldStat = binding.layoutStats.getChildAt(2); // Vị trí 2 là Items Sold
        itemsSoldStat.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("defaultTabIndex", 1); // 1 là tab "Sold"
            navController.navigate(R.id.action_global_to_myListingsFragment, args);
        });

        View followersStat = binding.layoutStats.getChildAt(4); // Vị trí 4 là Followers
        followersStat.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Followers list coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getHeaderState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ProfileHeaderState.Success) {
                bindHeaderData(((ProfileHeaderState.Success) state).user, ((ProfileHeaderState.Success) state).isCurrentUserProfile);
            } else if (state instanceof ProfileHeaderState.Error) {
                Toast.makeText(getContext(), ((ProfileHeaderState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getReportReviewEvent().observe(getViewLifecycleOwner(), event -> {
            Rating ratingToReport = event.getContentIfNotHandled();
            if (ratingToReport != null && isAdded()) {
                // *** SỬA LẠI LỜI GỌI - CHỈ 3 THAM SỐ ***
                // Khi report 1 review, reportedUserId là người đã viết ra cái review đó
                ReportContentDialogFragment.newInstance(
                        ratingToReport.getRatingId(),
                        "rating",
                        ratingToReport.getRaterUserId()
                ).show(getParentFragmentManager(), "ReportReviewDialog");
            }
        });
    }

    private void bindHeaderData(@NonNull User user, boolean isCurrentUserProfile) {
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        binding.textViewUserName.setText(user.getDisplayName());
        binding.ratingBarUser.setRating((float) user.getAverageRating());

        String reviewCountText = String.format(Locale.getDefault(), "%d Reviews", user.getTotalRatingCount());
        binding.textViewReviewCount.setText(reviewCountText);

        binding.textViewRatingValue.setText(String.format(Locale.getDefault(), "%.1f", user.getAverageRating()));

        binding.textStatListings.setText(String.valueOf(user.getTotalListings()));
        binding.textStatSold.setText(String.valueOf(user.getTotalTransactions()));

        // Ẩn/hiện nút dựa trên việc có phải hồ sơ của chính người dùng hay không
        int visibility = isCurrentUserProfile ? View.VISIBLE : View.GONE;
        binding.buttonEditProfile.setVisibility(visibility);
        binding.fabChangeProfilePicture.setVisibility(visibility);
        binding.layoutSecondaryActions.setVisibility(visibility);
        binding.buttonSettings.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}