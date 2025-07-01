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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentPublicProfileBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublicProfileFragment extends Fragment {

    private FragmentPublicProfileBinding binding;
    // Tái sử dụng ProfileViewModel. Hilt sẽ tự động cung cấp userId từ NavArgs cho ViewModel.
    private ProfileViewModel viewModel;
    private ProductAdapter productAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
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
        setupToolbar();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
        // TODO: Xử lý menu (Share, Report) nếu cần
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onItemClick(Item item) {
                // Khi người dùng đang ở trang profile của người khác và click vào 1 sản phẩm,
                // chúng ta điều hướng đến trang chi tiết của sản phẩm đó.
                if (isAdded() && item != null) {
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getItemId());
                    NavHostFragment.findNavController(PublicProfileFragment.this)
                            .navigate(R.id.action_global_to_itemDetailFragment, args);
                }
            }

            @Override
            public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {
                // TODO: Xử lý logic lưu sản phẩm sau
            }
        });
        // Layout simple hơn cho profile công khai
        binding.recyclerViewUserListings.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewUserListings.setAdapter(productAdapter);
        binding.recyclerViewUserListings.setNestedScrollingEnabled(false);
    }

    private void observeViewModel() {
        // Lắng nghe trạng thái header (chứa thông tin User)
        viewModel.getHeaderState().observe(getViewLifecycleOwner(), state -> {
            // TODO: Thêm xử lý cho trạng thái Loading
            if (state instanceof ProfileHeaderState.Success) {
                bindUserData(((ProfileHeaderState.Success) state).user);
            } else if (state instanceof ProfileHeaderState.Error) {
                Toast.makeText(getContext(), ((ProfileHeaderState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe danh sách sản phẩm của người dùng này
        viewModel.getActiveListings().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                productAdapter.submitList(items);
                // Cập nhật số lượng sản phẩm trên tiêu đề
                String listingsTitle = String.format(Locale.getDefault(), "Active Listings (%d)", items.size());
                binding.textViewListingsTitle.setText(listingsTitle);
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
        binding.toolbar.setTitle(user.getDisplayName()); // Cập nhật cả title của Toolbar

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
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}