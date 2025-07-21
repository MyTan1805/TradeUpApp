// File: src/main/java/com/example/tradeup/ui/profile/PublicProfileFragment.java
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
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.core.utils.UserRoleManager;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentPublicProfileBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.admin.AdminViewModel;
import com.example.tradeup.ui.report.ReportContentDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject; // *** THÊM IMPORT NÀY ***
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublicProfileFragment extends Fragment {

    private FragmentPublicProfileBinding binding;
    private ProfileViewModel profileViewModel; // ViewModel để lấy dữ liệu profile
    private AdminViewModel adminViewModel;   // ViewModel để thực hiện hành động admin
    private NavController navController;

    private PublicProfileTabsAdapter tabsAdapter;

    @Inject // *** BƯỚC 1: INJECT USERROLEMANAGER ***
    UserRoleManager userRoleManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // *** BƯỚC 2: KHỞI TẠO VIEWMODEL ĐÚNG CÁCH ***
        // Lấy từ scope của Fragment này để nhận NavArgs (userId)
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // Lấy từ scope của Activity để thực hiện hành động
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPublicProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        // Không gọi setupToolbar() ở đây nữa, vì nó cần dữ liệu user
        setupViewPagerAndTabs();
        observeViewModel();
    }

    // *** BƯỚC 3: SỬA LẠI SETUPTOOLBAR ĐỂ NHẬN USER ***
    private void setupToolbar(User user) {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        binding.toolbar.getMenu().clear(); // Xóa menu cũ để tránh trùng lặp

        if (userRoleManager.isAdmin()) {
            // Nếu người xem là admin, inflate menu của admin
            binding.toolbar.inflateMenu(R.menu.public_profile_admin_menu);
            binding.toolbar.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_change_role) {
                    showChangeRoleDialog(user);
                    return true;
                } else if (itemId == R.id.action_report_user) {
                    showReportDialog(user);
                    return true;
                }
                return false;
            });
        } else {
            // Inflate menu của user thường (chỉ có nút report)
            binding.toolbar.inflateMenu(R.menu.public_profile_menu);
            binding.toolbar.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_report_user) {
                    showReportDialog(user);
                    return true;
                }
                return false;
            });
        }
    }

    // Hàm tiện ích để hiển thị dialog report
    private void showReportDialog(User reportedUser) {
        ReportContentDialogFragment.newInstance(
                reportedUser.getUid(),
                "profile",
                reportedUser.getUid()
        ).show(getParentFragmentManager(), "ReportUserDialog");
    }

    private void setupViewPagerAndTabs() {
        tabsAdapter = new PublicProfileTabsAdapter(this);
        binding.viewPagerProfileContent.setAdapter(tabsAdapter);

        new TabLayoutMediator(binding.tabLayoutProfile, binding.viewPagerProfileContent,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Listings"); // Tên cho tab đầu tiên
                    } else {
                        tab.setText("Reviews");  // Tên cho tab thứ hai
                    }
                }
        ).attach();
    }

    // Hàm tiện ích để hiển thị dialog đổi vai trò
    private void showChangeRoleDialog(User user) {
        final CharSequence[] roles = {"user", "admin"};
        int currentRoleIndex = "admin".equalsIgnoreCase(user.getRole()) ? 1 : 0;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change User Role for " + user.getDisplayName())
                .setSingleChoiceItems(roles, currentRoleIndex, (dialog, which) -> {
                    String newRole = roles[which].toString();
                    adminViewModel.changeUserRole(user.getUid(), newRole);
                    dialog.dismiss();
                })
                .show();
    }

    private void observeViewModel() {
        // *** BƯỚC 4: SỬA LOGIC OBSERVER ***
        profileViewModel.getHeaderState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ProfileHeaderState.Success) {
                User user = ((ProfileHeaderState.Success) state).user;
                bindUserData(user);
                setupToolbar(user); // Cập nhật toolbar sau khi có user

                // Logic hiển thị nút Reactivate
                if (userRoleManager.isAdmin() && user.isDeactivated()) {
                    binding.buttonReactivate.setVisibility(View.VISIBLE);
                    binding.buttonReactivate.setOnClickListener(v -> {
                        adminViewModel.reactivateUser(user.getUid());
                        // Có thể thêm logic để tự động refresh lại trang sau khi reactivate
                    });
                } else {
                    binding.buttonReactivate.setVisibility(View.GONE);
                }
            } else if (state instanceof ProfileHeaderState.Error) {
                Toast.makeText(getContext(), ((ProfileHeaderState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe Toast từ AdminViewModel
        adminViewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // Nếu hành động thành công, tải lại dữ liệu profile để cập nhật UI
                if(message.contains("successfully")) {
                    profileViewModel.loadAllData();
                }
            }
        });
    }

    private void bindUserData(@NonNull User user) {
        // Tải ảnh đại diện
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        // Cập nhật tên trên header và toolbar
        binding.textViewUserName.setText(user.getDisplayName());
        binding.toolbar.setTitle(user.getDisplayName());

        // Cập nhật rating
        binding.ratingBarUser.setRating((float) user.getAverageRating());
        String ratingAndReviewsText = String.format(Locale.US, "%.1f (%d reviews)",
                user.getAverageRating(), user.getTotalRatingCount());
        binding.textViewRatingAndReviews.setText(ratingAndReviewsText);

        // Cập nhật Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            binding.textViewBio.setText(user.getBio());
            binding.textViewBio.setVisibility(View.VISIBLE);
        } else {
            binding.textViewBio.setVisibility(View.GONE);
        }

        // Cập nhật 3 chỉ số chính
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        binding.textViewUserName.setText(user.getDisplayName());
        binding.toolbar.setTitle(user.getDisplayName());

        binding.ratingBarUser.setRating((float) user.getAverageRating());

        if (user.getBio() != null && !user.getBio().isEmpty()) {
            binding.textViewBio.setText(user.getBio());
            binding.textViewBio.setVisibility(View.VISIBLE);
        } else {
            binding.textViewBio.setVisibility(View.GONE);
        }

        // *** CẬP NHẬT LOGIC BIND CHO 2 CHỈ SỐ ***
        // 1. Bind số lượng listings
        binding.textStatListings.setText(String.valueOf(user.getTotalListings()));

        // 2. Bind ngày tham gia
        if (user.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            binding.textStatMemberSince.setText(sdf.format(user.getCreatedAt().toDate()));
        } else {
            binding.textStatMemberSince.setText("N/A");
        }

        // Cập nhật nút Message
        String[] nameParts = user.getDisplayName().split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : user.getDisplayName();
        String messageButtonText = "Message " + firstName;
        binding.buttonMessage.setText(messageButtonText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}