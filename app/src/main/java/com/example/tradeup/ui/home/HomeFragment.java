package com.example.tradeup.ui.home;

import android.os.Bundle;
import android.util.Log;
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
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.databinding.FragmentHomeBinding;
import com.example.tradeup.ui.adapters.CategoryAdapter;
import com.example.tradeup.ui.adapters.ProductAdapter;
import java.util.Collections;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener, CategoryAdapter.OnCategoryClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private ProductAdapter recentItemsAdapter;
    private ProductAdapter featuredItemsAdapter;
    private ProductAdapter nearbyItemsAdapter;
    private CategoryAdapter categoryAdapter;
    private NavController navController;
    private ProductAdapter recommendedItemsAdapter;

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
        homeViewModel.refreshData();
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this);
        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        featuredItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_HORIZONTAL, this);
        binding.recyclerViewFeaturedItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFeaturedItems.setAdapter(featuredItemsAdapter);

        nearbyItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_HORIZONTAL, this);
        binding.recyclerViewNearbyItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewNearbyItems.setAdapter(nearbyItemsAdapter);

        recentItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewRecentListings.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewRecentListings.setAdapter(recentItemsAdapter);

        recommendedItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_HORIZONTAL, this);
        binding.recyclerViewRecommendedItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewRecommendedItems.setAdapter(recommendedItemsAdapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayoutHome.setOnRefreshListener(() -> homeViewModel.refreshData());
        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!v.canScrollVertically(1)) {
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
            boolean isListCurrentlyEmpty = recentItemsAdapter.getCurrentList() == null || recentItemsAdapter.getCurrentList().isEmpty();

            binding.progressBarHome.setVisibility(isLoading && isListCurrentlyEmpty ? View.VISIBLE : View.GONE);
            binding.swipeRefreshLayoutHome.setRefreshing(isLoading && !isListCurrentlyEmpty);
            binding.layoutEmptyStateHome.setVisibility(state instanceof HomeState.Empty ? View.VISIBLE : View.GONE);
            binding.buttonRetry.setVisibility(state instanceof HomeState.Error ? View.VISIBLE : View.GONE);

            if (state instanceof HomeState.Success) {
                HomeState.Success successState = (HomeState.Success) state;
                categoryAdapter.submitList(successState.categories);

                // Cập nhật trạng thái "đã lưu" trước khi submit list sản phẩm
                recentItemsAdapter.setSavedItemIds(successState.savedItemIds);
                recentItemsAdapter.submitList(successState.recentItems);

                featuredItemsAdapter.submitList(successState.recentItems.size() > 5 ? successState.recentItems.subList(0, 5) : successState.recentItems);

            } else if (state instanceof HomeState.Empty) {
                categoryAdapter.submitList(((HomeState.Empty) state).categories);
                recentItemsAdapter.submitList(Collections.emptyList());
                featuredItemsAdapter.submitList(Collections.emptyList());
            } else if (state instanceof HomeState.Error) {
                binding.textViewEmptyMessage.setText(((HomeState.Error) state).message);
                binding.layoutEmptyStateHome.setVisibility(View.VISIBLE);
            }
        });

        homeViewModel.getNearbyItems().observe(getViewLifecycleOwner(), nearbyItems -> {
            if (nearbyItems != null && !nearbyItems.isEmpty()) {
                nearbyItemsAdapter.submitList(nearbyItems);
                binding.layoutNearbyItems.setVisibility(View.VISIBLE);
            } else {
                binding.layoutNearbyItems.setVisibility(View.GONE);
            }
        });

        homeViewModel.getRecommendedItems().observe(getViewLifecycleOwner(), recommendedItems -> {
            if (recommendedItems != null && !recommendedItems.isEmpty()) {
                recommendedItemsAdapter.submitList(recommendedItems);
                binding.layoutRecommendedItems.setVisibility(View.VISIBLE);
            } else {
                binding.layoutRecommendedItems.setVisibility(View.GONE);
            }
        });

        homeViewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarLoadMore.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        homeViewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        // << XÓA BỎ OBSERVER THỪA Ở ĐÂY >>
    }

    @Override
    public void onItemClick(Item item) {
        if (isAdded() && item != null) {
            Bundle args = new Bundle();
            args.putString("itemId", item.getItemId());
            navController.navigate(R.id.action_global_to_itemDetailFragment, args);
        }
    }

    @Override
    public void onFavoriteClick(Item item) {
        homeViewModel.toggleFavoriteStatus(item);
    }

    @Override
    public void onCategoryClick(CategoryConfig category) {
        if (isAdded() && category != null) {
            Bundle args = new Bundle();
            args.putString("categoryId", category.getId());
            try {
                navController.navigate(R.id.action_global_to_categoryListingsFragment, args);
            } catch (Exception e) {
                Log.e("HomeFragment", "Navigation to CategoryListingsFragment failed", e);
                Toast.makeText(getContext(), "Could not open category.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}