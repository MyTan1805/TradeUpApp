package com.example.tradeup.ui.search;

import static android.content.ContentValues.TAG;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.databinding.FragmentSearchResultsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import com.example.tradeup.ui.search.SearchViewModel.SortOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import android.Manifest;
import androidx.core.content.ContextCompat;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchResultsFragment extends Fragment {

    private static final String REQUEST_KEY_CATEGORY = "search_category_request";
    private static final String REQUEST_KEY_CONDITION = "search_condition_request";

    private FragmentSearchResultsBinding binding;
    private SearchViewModel viewModel;
    private ProductAdapter searchResultsAdapter;
    private NavController navController;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // << THÊM: Khởi tạo launcher để xin quyền vị trí >>
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Nếu người dùng đồng ý cấp quyền, hiển thị lại dialog
                showDistanceDialog();
            } else {
                Toast.makeText(getContext(), "Location permission is required to search by distance.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
        setupFragmentResultListeners();

        if (getArguments() != null) {
            String initialQuery = SearchResultsFragmentArgs.fromBundle(getArguments()).getQuery();
            if (initialQuery != null && !initialQuery.isEmpty()) {
                binding.searchView.setQuery(initialQuery, false); // false = không submit
                viewModel.submitKeyword(initialQuery); // Gửi tới ViewModel để tìm kiếm ngay
            }
        }
    }

    private void setupRecyclerView() {
        searchResultsAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onItemClick(Item item) {
                Bundle args = new Bundle();
                args.putString("itemId", item.getItemId());
                navController.navigate(R.id.action_global_to_itemDetailFragment, args);
            }
            @Override
            public void onFavoriteClick(Item item) { /* TODO */ }
        });
        binding.recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.submitKeyword(query);
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setKeyword(newText);
                return true;
            }
        });

        binding.chipSort.setOnClickListener(v -> showSortDialog());
        binding.chipCategory.setOnClickListener(v -> showCategoryDialog());
        binding.chipCondition.setOnClickListener(v -> showConditionDialog());
        binding.chipPrice.setOnClickListener(v -> showPriceDialog());
        binding.chipDistance.setOnClickListener(v -> {
            // Kiểm tra quyền trước khi hiển thị dialog
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showDistanceDialog();
            } else {
                // Nếu chưa có quyền, xin quyền
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void observeViewModel() {

        viewModel.getScreenState().observe(getViewLifecycleOwner(), state -> {
            // Ẩn tất cả các view trạng thái trước
            binding.progressBarSearch.setVisibility(View.GONE);
            binding.recyclerViewSearchResults.setVisibility(View.GONE);
            binding.layoutEmptyStateSearch.setVisibility(View.GONE);

            if (state instanceof SearchScreenState.Loading) {
                binding.progressBarSearch.setVisibility(View.VISIBLE);
            } else if (state instanceof SearchScreenState.Success) {
                binding.recyclerViewSearchResults.setVisibility(View.VISIBLE);
                searchResultsAdapter.submitList(((SearchScreenState.Success) state).items);
            } else if (state instanceof SearchScreenState.Empty) {
                binding.layoutEmptyStateSearch.setVisibility(View.VISIBLE);
                searchResultsAdapter.submitList(Collections.emptyList()); // Xóa list cũ
            } else if (state instanceof SearchScreenState.Error) {
                // Có thể hiển thị một UI lỗi riêng thay vì Toast
                Toast.makeText(getContext(), ((SearchScreenState.Error) state).message, Toast.LENGTH_LONG).show();
                binding.layoutEmptyStateSearch.setVisibility(View.VISIBLE); // Hoặc UI lỗi
            }
        });

        viewModel.getSortOrder().observe(getViewLifecycleOwner(), sortOrder -> {
            if (sortOrder != null) {
                binding.chipSort.setText(sortOrder.displayName);
            }
        });

        viewModel.getErrorToast().observe(getViewLifecycleOwner(), event -> {
            String error = event.getContentIfNotHandled();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarSearch.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.recyclerViewSearchResults.setVisibility(View.GONE);
                binding.layoutEmptyStateSearch.setVisibility(View.GONE);
            }
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {
            if (viewModel.isLoading().getValue() != null && !viewModel.isLoading().getValue()) {
                boolean isEmpty = (items == null || items.isEmpty());
                binding.recyclerViewSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                binding.layoutEmptyStateSearch.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                searchResultsAdapter.submitList(items);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), event -> {
            String error = event.getContentIfNotHandled();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSortOrder().observe(getViewLifecycleOwner(), sortOrder -> {
            if (sortOrder != null) {
                binding.chipSort.setText(sortOrder.displayName);
            }
        });

        viewModel.getSelectedCategoryId().observe(getViewLifecycleOwner(), categoryId -> {
            if (categoryId == null || categoryId.isEmpty()) {
                binding.chipCategory.setText("Category");
            } else {
                if (viewModel.getAppConfig().getValue() != null) {
                    viewModel.getAppConfig().getValue().getCategories().stream() // <-- SỬA Ở ĐÂY
                            .filter(c -> c.getId().equals(categoryId))
                            .findFirst()
                            .ifPresent(c -> binding.chipCategory.setText(c.getName()));
                }
            }
        });


        viewModel.getDistanceInKm().observe(getViewLifecycleOwner(), distance -> {
            if (distance == null || distance <= 0) {
                binding.chipDistance.setText("Distance");
            } else {
                binding.chipDistance.setText(distance + " km");
            }
        });

        // Tương tự cho condition và price...
    }

    private void setupFragmentResultListeners() {
        FragmentManager fragmentManager = getParentFragmentManager();

        // Lắng nghe kết quả từ dialog chọn Category
        fragmentManager.setFragmentResultListener(REQUEST_KEY_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && viewModel.getAppConfig().getValue() != null) {
                if (index == 0) { // "All Categories"
                    viewModel.setCategoryAndSearch(null);
                } else {
                    CategoryConfig selected = viewModel.getAppConfig().getValue().getCategories().get(index - 1); // <-- SỬA Ở ĐÂY
                    viewModel.setCategoryAndSearch(selected.getId());
                }
            }
        });

        // Lắng nghe kết quả từ dialog chọn Condition
        fragmentManager.setFragmentResultListener(REQUEST_KEY_CONDITION, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && viewModel.getAppConfig().getValue() != null) {
                if (index == 0) { // "All Conditions"
                    viewModel.setConditionAndSearch(null);
                } else {
                    ItemConditionConfig selected = viewModel.getAppConfig().getValue().getItemConditions().get(index - 1);
                    viewModel.setConditionAndSearch(selected.getId());
                }
            }
        });
    }

    private void showSortDialog() {
        SearchViewModel.SortOrder[] options = SearchViewModel.SortOrder.values();
        String[] displayItems = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            displayItems[i] = options[i].displayName;
        }

        SearchViewModel.SortOrder currentOrder = viewModel.getSortOrder().getValue();
        int checkedItem = (currentOrder != null) ? currentOrder.ordinal() : 0;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sort By")
                .setSingleChoiceItems(displayItems, checkedItem, (dialog, which) -> {
                    viewModel.setSortOrderAndSearch(options[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void showCategoryDialog() {
        if (viewModel.getAppConfig().getValue() == null) return;
        ArrayList<String> names = (ArrayList<String>) viewModel.getAppConfig().getValue()
                .getCategories().stream() // <-- SỬA Ở ĐÂY
                .map(CategoryConfig::getName) // <-- SỬA Ở ĐÂY
                .collect(Collectors.toList());
        names.add(0, "All Categories");
        ListSelectionDialogFragment.newInstance("Select a Category", names, REQUEST_KEY_CATEGORY)
                .show(getParentFragmentManager(), "CategorySearchDialog");
    }

    private void showConditionDialog() {
        if (viewModel.getAppConfig().getValue() == null) return;
        ArrayList<String> names = (ArrayList<String>) viewModel.getAppConfig().getValue()
                .getItemConditions().stream()
                .map(ItemConditionConfig::getName)
                .collect(Collectors.toList());
        names.add(0, "All Conditions");
        ListSelectionDialogFragment.newInstance("Select Condition", names, REQUEST_KEY_CONDITION)
                .show(getParentFragmentManager(), "ConditionDialog");
    }

    private void showPriceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_price_filter, null);
        TextInputEditText minPriceInput = dialogView.findViewById(R.id.editTextMinPrice);
        TextInputEditText maxPriceInput = dialogView.findViewById(R.id.editTextMaxPrice);

        // Hiển thị giá trị hiện tại
        if(viewModel.getMinPrice().getValue() != null) minPriceInput.setText(String.valueOf(viewModel.getMinPrice().getValue()));
        if(viewModel.getMaxPrice().getValue() != null) maxPriceInput.setText(String.valueOf(viewModel.getMaxPrice().getValue()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Set Price Range")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String minStr = minPriceInput.getText() != null ? minPriceInput.getText().toString() : "";
                    String maxStr = maxPriceInput.getText() != null ? maxPriceInput.getText().toString() : "";
                    Double min = minStr.isEmpty() ? null : Double.parseDouble(minStr);
                    Double max = maxStr.isEmpty() ? null : Double.parseDouble(maxStr);
                    viewModel.setPriceRangeAndSearch(min, max);
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear", (dialog, which) -> viewModel.setPriceRangeAndSearch(null, null))
                .show();
    }

    private void showDistanceDialog() {
        final CharSequence[] distanceOptions = {"5 km", "10 km", "25 km", "50 km", "Any Distance"};
        final Integer[] distanceValues = {5, 10, 25, 50, null}; // Dùng null cho "Any"

        Integer currentDistance = viewModel.getDistanceInKm().getValue();
        int checkedItem = -1;
        for (int i = 0; i < distanceValues.length; i++) {
            if (Objects.equals(distanceValues[i], currentDistance)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Search Radius")
                .setSingleChoiceItems(distanceOptions, checkedItem, (dialog, which) -> {
                    viewModel.setDistanceAndSearch(distanceValues[which]);
                    dialog.dismiss();
                })
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}