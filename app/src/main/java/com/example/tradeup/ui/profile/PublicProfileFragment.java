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

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject; // *** THÊM IMPORT NÀY ***
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublicProfileFragment extends Fragment {

    private FragmentPublicProfileBinding binding;
    private ProfileViewModel profileViewModel; // ViewModel để lấy dữ liệu profile
    private AdminViewModel adminViewModel;   // ViewModel để thực hiện hành động admin
    private ProductAdapter productAdapter;
    private NavController navController;

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
        setupRecyclerView();
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


    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onItemClick(Item item) {
                if (isAdded() && item != null) {
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getItemId());
                    navController.navigate(R.id.action_global_to_itemDetailFragment, args);
                }
            }

            @Override
            public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {
                // TODO: Xử lý logic lưu sản phẩm sau
            }
        });
        binding.recyclerViewUserListings.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewUserListings.setAdapter(productAdapter);
        binding.recyclerViewUserListings.setNestedScrollingEnabled(false);
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

        profileViewModel.getActiveListings().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                productAdapter.submitList(items);
                String listingsTitle = String.format(Locale.getDefault(), "Active Listings (%d)", items.size());
                binding.textViewListingsTitle.setText(listingsTitle);
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
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        binding.textViewUserName.setText(user.getDisplayName());
        binding.toolbar.setTitle(user.getDisplayName());

        binding.ratingBarUser.setRating((float) user.getAverageRating());

        String ratingAndReviewsText = String.format(Locale.US, "%.1f (%d reviews)",
                user.getAverageRating(), user.getTotalRatingCount());
        binding.textViewRatingAndReviews.setText(ratingAndReviewsText);

        binding.textStatListings.setText(String.valueOf(user.getTotalListings()));

        if (user.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            binding.textStatMemberSince.setText(sdf.format(user.getCreatedAt().toDate()));
        } else {
            binding.textStatMemberSince.setText("N/A");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}