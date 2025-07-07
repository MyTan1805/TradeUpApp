// THAY THẾ TOÀN BỘ FILE: src/main/java/com/example/tradeup/ui/home/HomeFragment.java
package com.example.tradeup.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
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

import java.util.Collections;
import java.util.List;

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

    @Override
    public void onResume() {
        super.onResume();
        // Chỉ cần gọi refreshData, ViewModel sẽ xử lý phần còn lại
        homeViewModel.refreshData();
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this);
        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        featuredItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_HORIZONTAL, this);
        binding.recyclerViewFeaturedItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFeaturedItems.setAdapter(featuredItemsAdapter);

        // Không có nearbyItemsAdapter trong phiên bản này

        recentItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewRecentListings.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewRecentListings.setAdapter(recentItemsAdapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayoutHome.setOnRefreshListener(() -> homeViewModel.refreshData());

        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!v.canScrollVertically(1)) { // Kiểm tra đã cuộn đến cuối chưa
                homeViewModel.fetchMoreItems();
            }
        });

        binding.searchViewHome.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    Bundle args = new Bundle();
                    args.putString("query", query.trim());
                    navController.navigate(R.id.action_homeFragment_to_searchResultsFragment, args);
                    binding.searchViewHome.clearFocus();
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        binding.buttonRetry.setOnClickListener(v -> homeViewModel.refreshData());
    }

    private void observeViewModel() {
        homeViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            boolean isLoading = state instanceof HomeState.Loading;
            binding.progressBarHome.setVisibility(isLoading && recentItemsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            binding.swipeRefreshLayoutHome.setRefreshing(isLoading);

            binding.layoutEmptyStateHome.setVisibility(state instanceof HomeState.Empty ? View.VISIBLE : View.GONE);

            if (state instanceof HomeState.Success) {
                HomeState.Success successState = (HomeState.Success) state;
                categoryAdapter.submitList(successState.categories);
                recentItemsAdapter.submitList(successState.recentItems);

                List<Item> items = successState.recentItems;
                featuredItemsAdapter.submitList(items.size() > 5 ? items.subList(0, 5) : items);

            } else if (state instanceof HomeState.Empty) {
                categoryAdapter.submitList(((HomeState.Empty) state).categories);
                recentItemsAdapter.submitList(Collections.emptyList());
                featuredItemsAdapter.submitList(Collections.emptyList());

            } else if (state instanceof HomeState.Error) {
                binding.layoutEmptyStateHome.setVisibility(View.VISIBLE);
                binding.textViewEmptyMessage.setText(((HomeState.Error) state).message);
            }
        });

        homeViewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarLoadMore.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        homeViewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    // Các hàm OnClick listener giữ nguyên
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
        Toast.makeText(getContext(), "Favorite clicked on " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCategoryClick(DisplayCategoryConfig category) {
        if (isAdded() && category != null) {
            // TODO: Điều hướng đến màn hình danh sách sản phẩm theo category
            Toast.makeText(getContext(), "Navigate to category: " + category.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}