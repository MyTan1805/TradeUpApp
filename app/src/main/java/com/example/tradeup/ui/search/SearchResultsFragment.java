// File: src/main/java/com/example/tradeup/ui/search/SearchResultsFragment.java

package com.example.tradeup.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.databinding.FragmentSearchResultsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchResultsFragment extends Fragment {

    private static final String REQUEST_KEY_CATEGORY = "search_category_request";
    private static final String REQUEST_KEY_CONDITION = "search_condition_request";

    private FragmentSearchResultsBinding binding;
    private SearchViewModel viewModel;
    private ProductAdapter searchResultsAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
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
                binding.searchView.setQuery(initialQuery, false);
                viewModel.setKeywordAndSearch(initialQuery);
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
            public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) { /* TODO */ }
        });
        binding.recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setKeywordAndSearch(query);
                binding.searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        binding.chipCategory.setOnClickListener(v -> {
            if (viewModel.getAppConfig().getValue() != null) {
                ArrayList<String> categoryNames = (ArrayList<String>) viewModel.getAppConfig().getValue()
                        .getDisplayCategories().stream()
                        .map(DisplayCategoryConfig::getName)
                        .collect(Collectors.toList());
                categoryNames.add(0, "All Categories");

                ListSelectionDialogFragment.newInstance("Select a Category", categoryNames, REQUEST_KEY_CATEGORY)
                        .show(getParentFragmentManager(), "CategorySearchDialog");
            }
        });

        binding.chipCondition.setOnClickListener(v -> {
            if (viewModel.getAppConfig().getValue() != null) {
                ArrayList<String> conditionNames = (ArrayList<String>) viewModel.getAppConfig().getValue()
                        .getItemConditions().stream()
                        .map(ItemConditionConfig::getName)
                        .collect(Collectors.toList());
                conditionNames.add(0, "All Conditions");

                ListSelectionDialogFragment.newInstance("Select Condition", conditionNames, REQUEST_KEY_CONDITION)
                        .show(getParentFragmentManager(), "ConditionDialog");
            }
        });

        binding.chipPrice.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_price_filter, null);
            TextInputEditText minPriceInput = dialogView.findViewById(R.id.editTextMinPrice);
            TextInputEditText maxPriceInput = dialogView.findViewById(R.id.editTextMaxPrice);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Set Price Range")
                    .setView(dialogView)
                    .setPositiveButton("Apply", (dialog, which) -> {
                        String minPriceStr = minPriceInput.getText() != null ? minPriceInput.getText().toString() : "";
                        String maxPriceStr = maxPriceInput.getText() != null ? maxPriceInput.getText().toString() : "";
                        Double minPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
                        Double maxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
                        viewModel.setPriceRangeAndSearch(minPrice, maxPrice);
                    })
                    .setNegativeButton("Cancel", null)
                    .setNeutralButton("Clear", (dialog, which) -> viewModel.setPriceRangeAndSearch(null, null))
                    .show();
        });
    }

    private void setupFragmentResultListeners() {
        FragmentManager fragmentManager = getParentFragmentManager();

        fragmentManager.setFragmentResultListener(REQUEST_KEY_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && viewModel.getAppConfig().getValue() != null) {
                if (index == 0) {
                    viewModel.setCategoryAndSearch(null);
                } else {
                    DisplayCategoryConfig selected = viewModel.getAppConfig().getValue().getDisplayCategories().get(index - 1);
                    viewModel.setCategoryAndSearch(selected.getId());
                }
            }
        });

        fragmentManager.setFragmentResultListener(REQUEST_KEY_CONDITION, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && viewModel.getAppConfig().getValue() != null) {
                if (index == 0) {
                    viewModel.setConditionAndSearch(null);
                } else {
                    ItemConditionConfig selected = viewModel.getAppConfig().getValue().getItemConditions().get(index - 1);
                    viewModel.setConditionAndSearch(selected.getId());
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarSearch.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), items -> {
            boolean isEmpty = (items == null || items.isEmpty());
            if (viewModel.isLoading().getValue() != null && !viewModel.isLoading().getValue()) {
                binding.recyclerViewSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                binding.layoutEmptyStateSearch.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
            searchResultsAdapter.submitList(items);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), event -> {
            String error = event.getContentIfNotHandled();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSelectedCategoryId().observe(getViewLifecycleOwner(), categoryId -> {
            if (categoryId == null || categoryId.isEmpty()) {
                binding.chipCategory.setText("Category");
            } else {
                if (viewModel.getAppConfig().getValue() != null) {
                    viewModel.getAppConfig().getValue().getDisplayCategories().stream()
                            .filter(c -> c.getId().equals(categoryId))
                            .findFirst()
                            .ifPresent(c -> binding.chipCategory.setText(c.getName()));
                }
            }
        });

        viewModel.getSelectedConditionId().observe(getViewLifecycleOwner(), conditionId -> {
            if (conditionId == null || conditionId.isEmpty()) {
                binding.chipCondition.setText("Condition");
            } else {
                if (viewModel.getAppConfig().getValue() != null) {
                    viewModel.getAppConfig().getValue().getItemConditions().stream()
                            .filter(c -> c.getId().equals(conditionId))
                            .findFirst()
                            .ifPresent(c -> binding.chipCondition.setText(c.getName()));
                }
            }
        });

        viewModel.getMinPrice().observe(getViewLifecycleOwner(), minPrice -> {
            if (minPrice == null && viewModel.getMaxPrice().getValue() == null) {
                binding.chipPrice.setText("Price");
            } else {
                String priceText = (minPrice != null ? "$" + minPrice : "Any") + " - " +
                        (viewModel.getMaxPrice().getValue() != null ? "$" + viewModel.getMaxPrice().getValue() : "Any");
                binding.chipPrice.setText(priceText);
            }
        });

        viewModel.getMaxPrice().observe(getViewLifecycleOwner(), maxPrice -> {
            if (maxPrice == null && viewModel.getMinPrice().getValue() == null) {
                binding.chipPrice.setText("Price");
            } else {
                String priceText = (viewModel.getMinPrice().getValue() != null ? "$" + viewModel.getMinPrice().getValue() : "Any") + " - " +
                        (maxPrice != null ? "$" + maxPrice : "Any");
                binding.chipPrice.setText(priceText);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}