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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
            // Luôn ẩn/hiện progressbar dựa trên trạng thái Loading
            binding.progressBar.setVisibility(state instanceof ItemDetailViewState.Loading ? View.VISIBLE : View.GONE);
            // Ẩn/hiện nội dung chính
            setMainContentVisibility(state instanceof ItemDetailViewState.Success);

            if (state instanceof ItemDetailViewState.Success) {
                ItemDetailViewState.Success successState = (ItemDetailViewState.Success) state;
                populateUi(successState.item, successState.seller, successState.categoryName, successState.conditionName);
                setupClickListeners(successState.item, successState.seller);
            } else if (state instanceof ItemDetailViewState.Error) {
                Toast.makeText(getContext(), ((ItemDetailViewState.Error) state).message, Toast.LENGTH_LONG).show();
                if (isAdded()) {
                    // Nếu có lỗi nghiêm trọng (vd: không tìm thấy item), quay lại màn hình trước
                    navController.navigateUp();
                }
            }
        });

        viewModel.isBookmarked().observe(getViewLifecycleOwner(), this::updateBookmarkButton);

        viewModel.isViewingOwnItem().observe(getViewLifecycleOwner(), isOwnItem -> {
            if (isOwnItem != null) {
                // Ẩn thanh hành động dưới cùng nếu người dùng đang xem tin của chính mình
                binding.bottomActionBar.setVisibility(isOwnItem ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMainContentVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        binding.appBarLayout.setVisibility(visibility);
        binding.nestedScrollView.setVisibility(visibility);
        // Thanh bottom bar sẽ được quản lý bởi isViewingOwnItem observer
    }

    private void populateUi(@NonNull Item item, @NonNull User seller, @NonNull String categoryName, @NonNull String conditionName) {
        if (binding == null) return; // Đảm bảo view vẫn tồn tại

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
        }

        // Bind Item Info
        binding.toolbar.setTitle(item.getTitle());
        binding.textItemTitle.setText(item.getTitle());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.textItemPrice.setText(currencyFormat.format(item.getPrice()));
        binding.textItemCondition.setText(conditionName);
        binding.textItemDescription.setText(item.getDescription());

        // << FIX: HIỂN THỊ LƯỢT XEM >>
        if (item.getViewsCount() != null) {
            String viewsText = String.format(Locale.getDefault(), "%d views", item.getViewsCount());
            binding.textItemViews.setText(viewsText);
            binding.textItemViews.setVisibility(View.VISIBLE);
        } else {
            binding.textItemViews.setVisibility(View.GONE);
        }

        // Bind Seller Info
        Glide.with(this).load(seller.getProfilePictureUrl()).placeholder(R.drawable.ic_person).into(binding.imageSellerAvatar);
        binding.textSellerName.setText(seller.getDisplayName());
        if (item.getLocation() != null) {
            binding.textSellerLocation.setText(item.getLocation().getAddressString());
        }
        if (seller.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
            binding.textMemberSince.setText("Thành viên từ " + sdf.format(seller.getCreatedAt().toDate()));
        }

        // Bind Rating Info
        binding.ratingBarItem.setRating((float) seller.getAverageRating());
        String ratingInfo = String.format(Locale.US, "%.1f (%d đánh giá)", seller.getAverageRating(), seller.getTotalRatingCount());
        binding.textRatingInfo.setText(ratingInfo);
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
        // 1. Nút quay lại
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) navController.navigateUp();
        });

        // 2. Xử lý các item trong menu "ba chấm"
        binding.toolbar.setOnMenuItemClickListener(menuItem -> {
            int itemIdMenu = menuItem.getItemId();

            if (itemIdMenu == R.id.action_share) {
                Log.d(TAG, "Share menu item clicked.");
                // Logic chia sẻ của bạn
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "Hãy xem sản phẩm này trên TradeUp: " + item.getTitle() + "\nLink: https://tradeup.app/item/" + item.getItemId();
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
                return true; // Đã xử lý sự kiện

            } else if (itemIdMenu == R.id.action_report) {
                Log.d(TAG, "Report menu item clicked. Opening dialog...");
                // Logic mở Dialog báo cáo
                // Truyền cả ID người bán để ViewModel có thể lưu lại
                ReportContentDialogFragment.newInstance(item.getItemId(), "listing", item.getSellerId())
                        .show(getParentFragmentManager(), "ReportDialog");
                return true;
            }

            return false; // Chưa xử lý, để hệ thống xử lý tiếp (nếu có)
        });

        // 3. Nút Bookmark (đã là ImageButton riêng)
        binding.buttonBookmark.setOnClickListener(v -> viewModel.toggleBookmark());

        // 4. Khu vực thông tin người bán
        binding.sellerInfoContainer.setOnClickListener(v -> {
            Boolean isOwnItem = viewModel.isViewingOwnItem().getValue();
            if (isOwnItem != null) {
                if (isOwnItem) {
                    navController.navigate(R.id.navigation_profile);
                } else {
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
            Toast.makeText(getContext(), "Chức năng Nhắn tin sẽ được làm sau.", Toast.LENGTH_SHORT).show();
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