package com.example.tradeup.ui.details;

import android.content.Intent;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentItemDetailBinding;
import com.example.tradeup.ui.adapters.ImageSliderAdapter;
import com.example.tradeup.ui.offers.MakeOfferDialogFragment;
import com.example.tradeup.ui.report.ReportContentDialogFragment;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ItemDetailFragment extends Fragment {

    private static final String TAG = "ItemDetailFragment";
    private FragmentItemDetailBinding binding;
    private ItemDetailViewModel viewModel;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ItemDetailViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getViewState().observe(getViewLifecycleOwner(), state -> {
            if (binding == null) return;

            // Luôn ẩn/hiện progressbar dựa trên trạng thái Loading
            binding.progressBar.setVisibility(state instanceof ItemDetailViewState.Loading ? View.VISIBLE : View.GONE);
            // Ẩn/hiện nội dung chính
            setMainContentVisibility(state instanceof ItemDetailViewState.Success);

            if (state instanceof ItemDetailViewState.Success) {
                ItemDetailViewState.Success successState = (ItemDetailViewState.Success) state;
                populateUi(successState.item, successState.seller, successState.categoryName, successState.conditionName);
                setupClickListeners(successState.item, successState.seller);
                // Lưu lịch sử xem
                logUserBrowsingHistory(successState.item.getSellerId(), successState.item.getItemId(), successState.categoryName);
            } else if (state instanceof ItemDetailViewState.Error) {
                Toast.makeText(getContext(), ((ItemDetailViewState.Error) state).message, Toast.LENGTH_LONG).show();
                if (isAdded()) {
                    navController.navigateUp();
                }
            }
        });

        viewModel.isBookmarked().observe(getViewLifecycleOwner(), this::updateBookmarkButton);

        viewModel.isViewingOwnItem().observe(getViewLifecycleOwner(), isOwnItem -> {
            if (binding == null || isOwnItem == null) return;
            // Ẩn thanh hành động dưới cùng nếu người dùng đang xem tin của chính mình
            binding.bottomActionBar.setVisibility(isOwnItem ? View.GONE : View.VISIBLE);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigateToChatEvent().observe(getViewLifecycleOwner(), event -> {
            Bundle args = event.getContentIfNotHandled();
            if (args != null && isAdded()) {
                // Sử dụng action toàn cục để điều hướng từ bất kỳ đâu đến màn hình chat
                // Chúng ta cần tạo action này trong main_nav.xml
                try {
                    navController.navigate(R.id.action_global_to_chatDetailFragment, args);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to chat failed", e);
                    Toast.makeText(getContext(), "Could not open chat screen.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setMainContentVisibility(boolean visible) {
        if (binding == null) return;
        int visibility = visible ? View.VISIBLE : View.GONE;
        binding.appBarLayout.setVisibility(visibility);
        binding.nestedScrollView.setVisibility(visibility);
    }

    private void logUserBrowsingHistory(String userId, String itemId, String category) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> browsingData = new HashMap<>();
        browsingData.put("userId", userId);
        browsingData.put("itemId", itemId);
        browsingData.put("category", category);
        browsingData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("userBrowsingHistory")
                .add(browsingData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Browsing history logged"))
                .addOnFailureListener(e -> Log.e(TAG, "Error logging browsing history", e));
    }

    private void populateUi(@NonNull Item item, @NonNull User seller, @NonNull String categoryName, @NonNull String conditionName) {
        if (binding == null) return;

        // Setup Image Slider
        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(item.getImageUrls());
            binding.viewPagerImages.setAdapter(adapter);
            new TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerImages, (tab, position) -> {}).attach();
            updateImageCounter(1, item.getImageUrls().size());
            binding.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateImageCounter(position + 1, item.getImageUrls().size());
                }
            });
        } else {
            binding.viewPagerImages.setVisibility(View.GONE);
            binding.tabLayoutIndicator.setVisibility(View.GONE);
            binding.imageCounter.setVisibility(View.GONE);
        }

        // Bind Item Info
        binding.toolbar.setTitle(item.getTitle());
        binding.textItemTitle.setText(item.getTitle());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.textItemPrice.setText(currencyFormat.format(item.getPrice()));
        binding.textItemCondition.setText(conditionName);
        binding.textItemDescription.setText(item.getDescription());

        // Hiển thị lượt xem
        if (item.getViewsCount() != null) {
            String viewsText = String.format(Locale.getDefault(), "%d lượt xem", item.getViewsCount());
            binding.textItemViews.setText(viewsText);
            binding.textItemViews.setVisibility(View.VISIBLE);
        } else {
            binding.textItemViews.setVisibility(View.GONE);
        }

        // Bind Seller Info
        Glide.with(this)
                .load(seller.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageSellerAvatar);

        binding.textSellerName.setText(seller.getDisplayName());

        // Hiển thị rating
        binding.ratingBarSeller.setRating((float) seller.getAverageRating());
        String ratingCountText = String.format(Locale.getDefault(), "(%d reviews)", seller.getTotalRatingCount());
        binding.textSellerRatingCount.setText(ratingCountText);

        if (seller.getTotalRatingCount() > 0) {
            binding.ratingBarSeller.setVisibility(View.VISIBLE);
            binding.textSellerRatingCount.setVisibility(View.VISIBLE);
        } else {
            binding.ratingBarSeller.setVisibility(View.GONE);
            binding.textSellerRatingCount.setVisibility(View.GONE);
        }

        // === PHẦN LOGIC BIND ĐƯỢC THÊM LẠI ===
        // Hiển thị vị trí của người bán (lấy từ thông tin sản phẩm)
        binding.textSellerLocation.setText(item.getAddressString() != null ? item.getAddressString() : "N/A");
        binding.textSellerLocation.setVisibility(View.VISIBLE);

        // Hiển thị ngày tham gia của người bán
        if (seller.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            binding.textMemberSince.setText("Member since " + sdf.format(seller.getCreatedAt().toDate()));
            binding.textMemberSince.setVisibility(View.VISIBLE);
        } else {
            binding.textMemberSince.setVisibility(View.GONE);
        }
    }

    private void updateBookmarkButton(boolean isBookmarked) {
        if (binding == null) return;
        int drawableRes = isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border;
        binding.buttonBookmark.setImageResource(drawableRes);
    }

    private void updateImageCounter(int current, int total) {
        if (binding == null) return;
        binding.imageCounter.setText(String.format(Locale.getDefault(), "%d/%d", current, total));
    }

    private void setupClickListeners(@NonNull Item item, @NonNull User seller) {
        if (binding == null) return;

        // 1. Nút quay lại
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) navController.navigateUp();
        });

        // 2. Xử lý các item trong menu "ba chấm"
        binding.toolbar.setOnMenuItemClickListener(menuItem -> {
            int itemIdMenu = menuItem.getItemId();

            if (itemIdMenu == R.id.action_share) {
                Log.d(TAG, "Share menu item clicked.");
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "Hãy xem sản phẩm này trên TradeUp: " + item.getTitle() + "\nLink: https://tradeup.app/item/" + item.getItemId();
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
                return true;

            } else if (itemIdMenu == R.id.action_report) {
                Log.d(TAG, "Report menu item clicked. Opening dialog...");
                ReportContentDialogFragment.newInstance(item.getItemId(), "listing", item.getSellerId())
                        .show(getParentFragmentManager(), "ReportDialog");
                return true;
            }

            return false;
        });

        // 3. Nút Bookmark
        binding.buttonBookmark.setOnClickListener(v -> viewModel.toggleBookmark());

        // 4. Khu vực thông tin người bán
        binding.sellerInfoContainer.setOnClickListener(v -> {
            Boolean isOwnItem = viewModel.isViewingOwnItem().getValue();
            if (isOwnItem != null) {
                if (isOwnItem) {
                    // Nếu là sản phẩm của mình, có thể điều hướng về tab profile
                    navController.navigate(R.id.navigation_profile);
                } else {
                    // Nếu là của người khác, điều hướng đến trang public profile của họ
                    Bundle args = new Bundle();
                    args.putString("userId", seller.getUid());
                    navController.navigate(R.id.action_global_to_publicProfileFragment, args);
                }
            }
        });

        // 5. Nút Trả giá
        binding.buttonMakeOffer.setOnClickListener(v -> {
            MakeOfferDialogFragment.newInstance(item).show(getParentFragmentManager(), "MakeOfferDialog");
        });

        // 6. Nút Nhắn tin
        binding.buttonMessageSeller.setOnClickListener(v -> {
            viewModel.onMessageSellerClicked();
        });
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.viewPagerImages.setAdapter(null);
        }
        super.onDestroyView();
        binding = null;
    }
}