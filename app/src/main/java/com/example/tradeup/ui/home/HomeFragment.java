// File: src/main/java/com/example/tradeup/ui/home/HomeFragment.java
package com.example.tradeup.ui.home;

import static androidx.fragment.app.FragmentManager.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private ViewTreeObserver.OnScrollChangedListener scrollChangedListener;

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
        setupObservers();
        setupListeners(); // Giữ nguyên lời gọi ở đây
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

    private void setupObservers() {
        // Quan sát trạng thái loading
        homeViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                // Chỉ hiển thị ProgressBar chính khi danh sách rỗng, ngược lại hiển thị ProgressBar load more
                boolean isListEmpty = recentItemsAdapter.getItemCount() == 0;
                binding.progressBarHome.setVisibility(isLoading && isListEmpty ? View.VISIBLE : View.GONE);
                binding.progressBarLoadMore.setVisibility(isLoading && !isListEmpty ? View.VISIBLE : View.GONE);

                // Vô hiệu hóa SwipeRefreshLayout khi đang tải
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
            recentItemsAdapter.submitList(items);
            // Lấy 5 item đầu tiên làm sản phẩm nổi bật
            featuredItemsAdapter.submitList(items != null && items.size() > 5 ? items.subList(0, 5) : items);
            // Hiển thị/ẩn trạng thái rỗng
            binding.layoutEmptyStateHome.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Quan sát thông báo lỗi
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefreshLayoutHome.setOnRefreshListener(() -> homeViewModel.fetchRecentItems(true));

        // Tạo listener và lưu vào biến thành viên
        scrollChangedListener = () -> {
            // Thêm kiểm tra binding != null để tăng độ an toàn
            if (binding == null) return;

            View view = binding.swipeRefreshLayoutHome.getChildAt(0);
            if (view == null) return;

            int diff = (view.getBottom() - (binding.swipeRefreshLayoutHome.getHeight() + binding.swipeRefreshLayoutHome.getScrollY()));
            if (diff == 0) {
                if (Boolean.FALSE.equals(homeViewModel.isLoading().getValue()) && !homeViewModel.isLastPage()) {
                    homeViewModel.fetchRecentItems(false);
                }
            }
        };
        // Gán listener
        binding.swipeRefreshLayoutHome.getViewTreeObserver().addOnScrollChangedListener(scrollChangedListener);

        binding.searchBarContainer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng tìm kiếm sắp ra mắt!", Toast.LENGTH_SHORT).show();
        });
    }

    // --- Triển khai các Callbacks từ Adapters ---

    @Override
    public void onItemClick(Item item) {
        if (item == null || item.getItemId() == null || item.getItemId().isEmpty()) {
            Toast.makeText(getContext(), "Sản phẩm không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- CÁCH SỬA LỖI ---

        // 1. Tạo một đối tượng Bundle để chứa các tham số
        Bundle args = new Bundle();

        // 2. Đặt itemId vào Bundle với key khớp với tên argument trong nav_graph
        // Tên argument của bạn là "itemId"
        args.putString("itemId", item.getItemId());

        // 3. Gọi navigate với ID của action và Bundle chứa tham số
        // Action ID của bạn là "action_homeFragment_to_itemDetailFragment"
        if (isAdded() && navController != null) {
            try {
                navController.navigate(R.id.action_homeFragment_to_itemDetailFragment, args);
            } catch (Exception e) {
                // Phòng trường hợp action không được tìm thấy
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
        // TODO: Gọi ViewModel để lọc sản phẩm theo category
        // homeViewModel.filterByCategory(category.getId());

    }

    @Override
    public void onDestroyView() {
        // === FIX: GỠ BỎ LISTENER TRƯỚC KHI SET BINDING = NULL ===
        if (binding != null && scrollChangedListener != null) {
            binding.swipeRefreshLayoutHome.getViewTreeObserver().removeOnScrollChangedListener(scrollChangedListener);
        }

        super.onDestroyView();
        binding = null;
        scrollChangedListener = null;
    }
}