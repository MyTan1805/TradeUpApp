// File: src/main/java/com/example/tradeup/ui/home/HomeFragment.java

package com.example.tradeup.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.databinding.FragmentHomeBinding;
import com.example.tradeup.ui.adapters.CategoryAdapter;
import com.example.tradeup.ui.adapters.ProductAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener, CategoryAdapter.OnCategoryClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private ProductAdapter recentItemsAdapter;
    private ProductAdapter featuredItemsAdapter;
    private CategoryAdapter categoryAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerViews();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(this);
        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        // Featured Items RecyclerView
        featuredItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_HORIZONTAL, this);
        binding.recyclerViewFeaturedItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFeaturedItems.setAdapter(featuredItemsAdapter);

        // Recent Listings RecyclerView
        recentItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewRecentListings.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewRecentListings.setAdapter(recentItemsAdapter);
        binding.recyclerViewRecentListings.setNestedScrollingEnabled(false); // Quan trọng khi nằm trong NestedScrollView
    }

    private void setupListeners() {
        // Kéo để làm mới
        binding.swipeRefreshLayoutHome.setOnRefreshListener(() -> {
            homeViewModel.fetchRecentItems(true); // Gọi hàm refresh trong ViewModel
        });

        // Lắng nghe sự kiện cuộn của NestedScrollView để tải thêm
        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Kiểm tra nếu đã cuộn đến cuối
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                // Chỉ gọi tải thêm khi không đang tải và chưa phải trang cuối
                if (!homeViewModel.isLoadingMore() && !homeViewModel.isLastPage()) {
                    homeViewModel.fetchRecentItems(false); // Gọi hàm tải thêm
                }
            }
        });

        binding.searchViewHome.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty()) {
                    // Tạo bundle và điều hướng
                    Bundle args = new Bundle();
                    args.putString("query", query);
                    navController.navigate(R.id.action_homeFragment_to_searchResultsFragment, args);
                    binding.searchViewHome.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        binding.buttonRetry.setOnClickListener(v -> {
            homeViewModel.fetchRecentItems(true);
        });
    }

    private void observeViewModel() {
        // Quan sát trạng thái loading
        homeViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                // Hiển thị ProgressBar chính khi tải lần đầu và danh sách rỗng
                boolean isListCurrentlyEmpty = homeViewModel.getRecentItems().getValue() == null || homeViewModel.getRecentItems().getValue().isEmpty();
                binding.progressBarHome.setVisibility(isLoading && isListCurrentlyEmpty ? View.VISIBLE : View.GONE);

                // Ẩn icon refreshing của SwipeRefreshLayout khi tải xong
                if (!isLoading) {
                    binding.swipeRefreshLayoutHome.setRefreshing(false);
                }
            }
        });

        // Quan sát danh sách categories
        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.submitList(categories);
            }
        });

        // Quan sát danh sách sản phẩm
        homeViewModel.getRecentItems().observe(getViewLifecycleOwner(), items -> {
            if (items == null) return;

            // Cập nhật adapter sản phẩm gần đây
            recentItemsAdapter.submitList(items);

            // Cập nhật adapter sản phẩm nổi bật (lấy 5 sản phẩm đầu tiên)
            if (items.size() > 5) {
                featuredItemsAdapter.submitList(items.subList(0, 5));
            } else {
                featuredItemsAdapter.submitList(items);
            }

            // Hiển thị/ẩn trạng thái rỗng
            boolean isEmpty = items.isEmpty();
            binding.layoutEmptyStateHome.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.buttonRetry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

            // Ẩn ProgressBar Load More sau khi cập nhật danh sách
            binding.progressBarLoadMore.setVisibility(View.GONE);
        });

        // Quan sát thông báo lỗi
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), event -> {
            String error = event.getContentIfNotHandled();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                // Ẩn các icon loading nếu có lỗi
                binding.swipeRefreshLayoutHome.setRefreshing(false);
                binding.progressBarHome.setVisibility(View.GONE);
                binding.progressBarLoadMore.setVisibility(View.GONE);
            }
        });
    }

    // --- Triển khai các Callbacks từ Adapters ---

    @Override
    public void onItemClick(Item item) {
        if (item == null || item.getItemId() == null || item.getItemId().isEmpty()) {
            Toast.makeText(getContext(), "Sản phẩm không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAdded() && navController != null) {
            try {
                Bundle args = new Bundle();
                args.putString("itemId", item.getItemId());
                navController.navigate(R.id.action_global_to_itemDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Không thể mở chi tiết sản phẩm.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {
        Toast.makeText(getContext(), "Lưu: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Gọi ViewModel để xử lý lưu/bỏ lưu sản phẩm
        // homeViewModel.toggleFavorite(item);
    }

    @Override
    public void onCategoryClick(DisplayCategoryConfig category) {
        Toast.makeText(getContext(), "Lọc theo: " + category.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Điều hướng đến màn hình danh sách sản phẩm theo danh mục
        // Bundle args = new Bundle();
        // args.putString("categoryId", category.getId());
        // navController.navigate(R.id.action_global_to_categoryListingsFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Gỡ bỏ tham chiếu đến binding để tránh memory leak
        binding = null;
    }
}