package com.example.tradeup.ui.saved;

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
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentSavedItemsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SavedItemsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "SavedItemsFragment";

    private FragmentSavedItemsBinding binding;
    private SavedItemsViewModel viewModel;
    private ProductAdapter savedItemsAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SavedItemsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedItemsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        // TODO: Thêm listener cho menu nếu cần
    }

    private void setupRecyclerView() {
        // Tái sử dụng ProductAdapter với kiểu hiển thị dạng lưới
        savedItemsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewSavedItems.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewSavedItems.setAdapter(savedItemsAdapter);
    }

    private void setupListeners() {
        // Xử lý sự kiện click vào các chip filter
        binding.chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAllItems) {
                // TODO: Gọi hàm viewModel.onSortByDefault()
            } else if (checkedId == R.id.chipRecentlySaved) {
                // TODO: Gọi hàm viewModel.onSortByDate()
            } else if (checkedId == R.id.chipPriceLowHigh) {
                // TODO: Gọi hàm viewModel.onSortByPrice()
            }
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Hiển thị ProgressBar nếu cần
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getSavedItems().observe(getViewLifecycleOwner(), items -> {
            Log.d(TAG, "Observer received " + (items != null ? items.size() : 0) + " saved items.");
            savedItemsAdapter.submitList(items);

            // TODO: Hiển thị trạng thái empty view
            // binding.emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Implement Callbacks từ ProductAdapter ---

    @Override
    public void onItemClick(Item item) {
        // Điều hướng đến trang chi tiết sản phẩm
        if (isAdded() && item != null) {
            Bundle args = new Bundle();
            args.putString("itemId", item.getItemId());
            navController.navigate(R.id.action_global_to_itemDetailFragment, args);
        }
    }

    @Override
    public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {
        // Trong màn hình này, click vào nút favorite luôn là hành động "unsave"
        Log.d(TAG, "Unsaving item: " + item.getTitle());
        viewModel.unsaveItem(item);
    }
}