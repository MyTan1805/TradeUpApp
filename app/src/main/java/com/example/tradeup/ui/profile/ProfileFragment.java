package com.example.tradeup.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
// import androidx.core.view.ViewKt; // Không cần nếu bạn set visibility trực tiếp
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
// import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.ContactInfo;
import com.example.tradeup.data.model.Item; // Cần cho List<Item>
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentProfileBinding;
import com.example.tradeup.ui.auth.AuthViewModel;
// import com.example.tradeup.ui.adapters.UserItemListAdapter;


import java.text.DecimalFormat;
import java.util.List; // Cần cho List<Item>

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment implements MenuProvider {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;

    // private UserItemListAdapter userItemListAdapter;

    private static final String TAG = "ProfileFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // Lấy AuthViewModel với scope của Activity để xử lý logout chung
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        Log.d(TAG, "onCreate called");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Profile User ID Arg from ViewModel: " + profileViewModel.getProfileUserIdArg());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called. Profile User ID Arg: " + profileViewModel.getProfileUserIdArg() +
                ", Current Auth UID: " + profileViewModel.getCurrentAuthUserUid());

        setupToolbarAndMenu();
        // setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // ViewModel đã tự load trong init, hoặc bạn có thể trigger lại nếu cần refresh từ Fragment
        // profileViewModel.loadUserProfile();
    }

    private void setupToolbarAndMenu() {
        // Nút back trên toolbar chỉ nên có nếu đây là public profile
        // Sẽ được xử lý trong onPrepareMenu hoặc khi bindUserData

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.profile_toolbar_menu, menu);
    }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        ProfileState currentState = profileViewModel.getProfileState().getValue();
        boolean isCurrentUserProfile = false;
        boolean showMenuOptions = false;

        if (currentState instanceof ProfileState.Success) {
            ProfileState.Success successState = (ProfileState.Success) currentState;
            isCurrentUserProfile = successState.isCurrentUserProfile;
            showMenuOptions = true; // Hiển thị menu khi load thành công
        }

        MenuItem editItem = menu.findItem(R.id.action_edit_profile);
        if (editItem != null) editItem.setVisible(showMenuOptions && isCurrentUserProfile);

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (settingsItem != null) settingsItem.setVisible(showMenuOptions && isCurrentUserProfile);

        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        if (logoutItem != null) logoutItem.setVisible(showMenuOptions && isCurrentUserProfile);

        MenuItem reportItem = menu.findItem(R.id.action_report_user);
        if (reportItem != null) reportItem.setVisible(showMenuOptions && !isCurrentUserProfile);
    }


    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_edit_profile) {
            Log.d(TAG, "Edit Profile clicked");
            if (isAdded()) {
                // Truyền ID của user hiện tại (nếu đang xem profile của mình)
                // hoặc ID của user đang xem (nếu thiết kế cho phép admin sửa)
                // Hiện tại, EditProfileFragment sẽ tự lấy thông tin user hiện tại từ ProfileViewModel.currentUserForEdit
                NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_editProfileFragment);
            }
            return true;
        } else if (itemId == R.id.action_settings) {
            Log.d(TAG, "Settings clicked");
            Toast.makeText(requireContext(), "Cài đặt", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_report_user) {
            ProfileState currentState = profileViewModel.getProfileState().getValue();
            if (currentState instanceof ProfileState.Success) {
                User reportedUser = ((ProfileState.Success) currentState).user;
                Log.d(TAG, "Report User clicked for UID: " + reportedUser.getUid());
                // TODO: Show report user dialog/fragment, truyền reportedUser.getUid()
                Toast.makeText(requireContext(), "Báo cáo người dùng: " + reportedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.action_logout) {
            Log.d(TAG, "Logout clicked");
            authViewModel.logoutUser();
            return true;
        }
        return false;
    }

    /*
    private void setupRecyclerView() {
        // ...
    }
    */

    private void setupClickListeners() {
        binding.buttonActionTopRight.setOnClickListener(v -> {
            ProfileState currentState = profileViewModel.getProfileState().getValue();
            if (currentState instanceof ProfileState.Success && ((ProfileState.Success) currentState).isCurrentUserProfile) {
                Log.d(TAG, "Edit Profile button (top right) clicked");
                if (isAdded()) {
                    NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_editProfileFragment);
                }
            }
            // Nếu là public profile và nút này là "Theo dõi" thì xử lý ở đây
        });

        binding.buttonSendMessage.setOnClickListener(v -> {
            ProfileState currentState = profileViewModel.getProfileState().getValue();
            if (currentState instanceof ProfileState.Success && !((ProfileState.Success) currentState).isCurrentUserProfile) {
                User otherUser = ((ProfileState.Success) currentState).user;
                Log.d(TAG, "Send Message button clicked to user: " + otherUser.getUid());
                // TODO: Navigate to ChatFragment with otherUser.getUid()
                Toast.makeText(requireContext(), "Nhắn tin tới " + otherUser.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.textViewMyListingsLink.setOnClickListener(v -> {
            Log.d(TAG, "My Listings link clicked");
            // TODO: Navigate to user's listings screen (MyListingsFragment)
            // Ví dụ: NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_myListingsFragment);
            Toast.makeText(requireContext(), "Sản phẩm của tôi", Toast.LENGTH_SHORT).show();
        });
        binding.textViewTransactionHistoryLink.setOnClickListener(v -> {
            Log.d(TAG, "Transaction History link clicked");
            Toast.makeText(requireContext(), "Lịch sử giao dịch", Toast.LENGTH_SHORT).show();
        });
        binding.textViewSavedItemsLink.setOnClickListener(v -> {
            Log.d(TAG, "Saved Items link clicked");
            Toast.makeText(requireContext(), "Sản phẩm đã lưu", Toast.LENGTH_SHORT).show();
        });
    }


    private void setupObservers() {
        profileViewModel.getProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            Log.d(TAG, "ProfileState observed: " + state.getClass().getSimpleName());

            binding.progressBarProfile.setVisibility(state instanceof ProfileState.Loading ? View.VISIBLE : View.GONE);
            binding.profileContentContainer.setVisibility(state instanceof ProfileState.Success ? View.VISIBLE : View.INVISIBLE);

            if (state instanceof ProfileState.Success) {
                ProfileState.Success successState = (ProfileState.Success) state;
                bindUserData(successState.user, successState.isCurrentUserProfile);
                // if (userItemListAdapter != null) {
                //     userItemListAdapter.submitList(successState.items);
                // }
                Log.d(TAG, "User items count: " + successState.items.size());
                requireActivity().invalidateOptionsMenu(); // Yêu cầu vẽ lại menu để cập nhật visibility
            } else if (state instanceof ProfileState.Error) {
                ProfileState.Error errorState = (ProfileState.Error) state;
                Toast.makeText(requireContext(), "Lỗi: " + errorState.message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error state: " + errorState.message);
                // Hiển thị layout lỗi thay vì để trống
                // binding.layoutErrorState.setVisibility(View.VISIBLE);
                // binding.textViewErrorMessage.setText(errorState.message);
            } else if (state instanceof ProfileState.Idle) {
                Log.d(TAG, "ProfileState.Idle");
                // Có thể ẩn content và hiện loading nếu Idle nghĩa là chưa load
                // binding.profileContentContainer.setVisibility(View.INVISIBLE);
                // binding.progressBarProfile.setVisibility(View.VISIBLE);
                // profileViewModel.loadUserProfile(); // Trigger load nếu đang Idle
            } else if (state instanceof ProfileState.Loading) {
                Log.d(TAG, "ProfileState.Loading");
                // Đã xử lý
            }
        });

        authViewModel.getLogoutState().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut != null && isLoggedOut) {
                Log.d(TAG, "Logout successful, navigating.");
                Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                try {
                    if (isAdded()) {
                        NavController navController = NavHostFragment.findNavController(this);
                        // Điều hướng về màn hình bắt đầu của luồng auth (thường là Login)
                        // và xóa hết backstack của profile/main.
                        // Cần có một action global hoặc một nested graph cho auth.
                        // Ví dụ: nếu nav_graph chính có ID là R.id.main_nav_graph
                        // và loginFragment là start destination của nó.
                        // Hoặc nếu bạn có một nav_graph riêng cho auth.
                        // Đây là một ví dụ đơn giản, bạn cần điều chỉnh cho đúng cấu trúc nav của mình.
                        navController.popBackStack(R.id.loginFragment, false); // Pop về login, không inclusive
                        // Hoặc
                        // navController.navigate(R.id.your_login_or_auth_start_destination_action);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating after logout", e);
                }
                authViewModel.onLogoutCompleted();
            }
        });
    }

    private void bindUserData(User user, boolean isCurrentUserProfile) {
        if (binding == null || user == null) {
            Log.w(TAG, "Binding or User is null in bindUserData");
            return;
        }

        // Cập nhật Toolbar title và navigation icon
        if (isCurrentUserProfile) {
            binding.toolbarProfile.setTitle(getString(R.string.ho_so_cua_toi));
            binding.toolbarProfile.setNavigationIcon(null); // Không có nút back cho profile của mình
        } else {
            binding.toolbarProfile.setTitle(user.getDisplayName());
            binding.toolbarProfile.setNavigationIcon(R.drawable.ic_arrow_back); // Nút back cho public profile
            binding.toolbarProfile.setNavigationOnClickListener(v -> {
                if (isAdded()) NavHostFragment.findNavController(this).popBackStack();
            });
        }


        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        binding.textViewDisplayName.setText(user.getDisplayName());

        boolean shouldShowEmail = isCurrentUserProfile && user.getEmail() != null && !user.getEmail().isEmpty();
        binding.textViewEmail.setVisibility(shouldShowEmail ? View.VISIBLE : View.GONE);
        if (shouldShowEmail) {
            binding.textViewEmail.setText(user.getEmail());
        }

        String bio = user.getBio();
        boolean shouldShowBio = (bio != null && !bio.isEmpty()) || isCurrentUserProfile;
        binding.textViewBio.setVisibility(shouldShowBio ? View.VISIBLE : View.GONE);
        if (shouldShowBio) {
            binding.textViewBio.setText(bio != null && !bio.isEmpty() ? bio : (isCurrentUserProfile ? "Chưa có tiểu sử." : ""));
        }


        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double avgRating = user.getAverageRating();
        binding.textViewAverageRating.setText(avgRating > 0 ? decimalFormat.format(avgRating) + " ★" : "Chưa có");
        binding.textViewTotalTransactions.setText(String.valueOf(user.getTotalTransactions()));
        binding.textViewTotalListings.setText(String.valueOf(user.getTotalListings()));


        ContactInfo contactInfo = user.getContactInfo();
        boolean hasAnyContactInfo = false;
        if (contactInfo != null && contactInfo.getPhone() != null && !contactInfo.getPhone().isEmpty()) {
            binding.textViewPhoneNumber.setText("SĐT: " + contactInfo.getPhone());
            binding.textViewPhoneNumber.setVisibility(View.VISIBLE);
            hasAnyContactInfo = true;
        } else {
            binding.textViewPhoneNumber.setVisibility(View.GONE);
        }
        // TODO: Hiển thị Zalo, Facebook tương tự nếu có và cập nhật hasAnyContactInfo
        binding.labelContactInfo.setVisibility(isCurrentUserProfile && hasAnyContactInfo ? View.VISIBLE : View.GONE);


        binding.layoutMyProfileActions.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.GONE);
        binding.buttonSendMessage.setVisibility(!isCurrentUserProfile ? View.VISIBLE : View.GONE);

        if (isCurrentUserProfile) {
            binding.buttonActionTopRight.setText(getString(R.string.chinh_sua_ho_so_title));
            binding.buttonActionTopRight.setIconResource(R.drawable.ic_edit);
        } else {
            // TODO: Logic cho nút action trên public profile (ví dụ: Theo dõi/Báo cáo sớm)
            // Hiện tại ẩn đi nếu là public profile, vì menu item đã có Report
            binding.buttonActionTopRight.setVisibility(View.GONE);
            // binding.buttonActionTopRight.setText("Theo dõi");
            // binding.buttonActionTopRight.setIconResource(R.drawable.ic_follow); // Cần icon
        }
        // Chỉ hiện nút action top right nếu là profile của mình
        binding.buttonActionTopRight.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.GONE);


        boolean shouldShowUserListings = !isCurrentUserProfile; // Chỉ hiển thị sản phẩm nếu là public profile
        binding.textViewUserListingsTitle.setVisibility(shouldShowUserListings ? View.VISIBLE : View.GONE);
        binding.recyclerViewUserListings.setVisibility(shouldShowUserListings ? View.VISIBLE : View.GONE);
        if (shouldShowUserListings) {
            binding.textViewUserListingsTitle.setText(getString(R.string.san_pham_cua_prefix) + " " + user.getDisplayName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}