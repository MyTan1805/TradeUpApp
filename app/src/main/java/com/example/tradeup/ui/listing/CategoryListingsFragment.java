// File: src/main/java/com/example/tradeup/ui/listing/CategoryListingsFragment.java
package com.example.tradeup.ui.listing;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.model.config.SubcategoryConfig;
import com.example.tradeup.databinding.FragmentCategoryListingsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.google.android.material.chip.Chip;
import java.util.Comparator;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryListingsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentCategoryListingsBinding binding;
    private CategoryViewModel viewModel;
    private ProductAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        // Title sẽ được cập nhật động từ ViewModel
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        linearLayoutManager = new LinearLayoutManager(getContext());

        binding.recyclerViewCategoryResults.setLayoutManager(gridLayoutManager);
        binding.recyclerViewCategoryResults.setAdapter(adapter);
    }

    private void setupListeners() {
        // Lắng nghe sự kiện chọn chip
        binding.chipGroupSubCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                // Khi bỏ chọn tất cả, mặc định hiển thị "All"
                viewModel.filterBySubCategory(null);
                return;
            }
            Chip checkedChip = group.findViewById(checkedId);
            if (checkedChip != null) {
                // Lấy ID đã lưu trong tag của chip
                String subCategoryId = (String) checkedChip.getTag();
                viewModel.filterBySubCategory(subCategoryId);
            }
        });

        // Lắng nghe sự kiện click sắp xếp
        binding.textViewSortBy.setOnClickListener(v -> {
            // Đây là ví dụ đơn giản, bạn có thể thay bằng một dialog
            viewModel.sortItems(Comparator.comparingDouble(Item::getPrice));
            Toast.makeText(getContext(), "Sorted by price (asc)", Toast.LENGTH_SHORT).show();
        });

        // Lắng nghe sự kiện đổi chế độ xem
        binding.toggleGroupViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonGridView) {
                    binding.recyclerViewCategoryResults.setLayoutManager(gridLayoutManager);
                } else if (checkedId == R.id.buttonListView) {
                    binding.recyclerViewCategoryResults.setLayoutManager(linearLayoutManager);
                }
            }
        });
        binding.toggleGroupViewMode.check(R.id.buttonGridView); // Set mặc định
    }

    private void observeViewModel() {
        // Lắng nghe thông tin danh mục cha để tạo chip và đặt title
        viewModel.getParentCategory().observe(getViewLifecycleOwner(), parentCategory -> {
            if (parentCategory != null) {
                binding.toolbar.setTitle(parentCategory.getName());
                createSubCategoryChips(parentCategory);
            }
        });

        // Lắng nghe danh sách sản phẩm để hiển thị
        viewModel.getDisplayedItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.submitList(items);
                binding.emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm để tạo các chip danh mục con một cách động
    private void createSubCategoryChips(CategoryConfig parentCategory) {
        if (getContext() == null) return;
        binding.chipGroupSubCategories.removeAllViews();

        // 1. Tạo chip "All"
        Chip allChip = (Chip) getLayoutInflater().inflate(R.layout.chip_choice_layout, binding.chipGroupSubCategories, false);
        allChip.setText(getString(R.string.category_listings_all_prefix, parentCategory.getName()));
        allChip.setTag(null); // Tag là null để ViewModel hiểu là hiển thị tất cả
        binding.chipGroupSubCategories.addView(allChip);

        // 2. Tạo các chip cho từng danh mục con
        if (parentCategory.getSubcategories() != null) {
            for (SubcategoryConfig sub : parentCategory.getSubcategories()) {
                Chip subChip = (Chip) getLayoutInflater().inflate(R.layout.chip_choice_layout, binding.chipGroupSubCategories, false);
                subChip.setText(sub.getName());
                subChip.setTag(sub.getId()); // Dùng Tag để lưu ID
                binding.chipGroupSubCategories.addView(subChip);
            }
        }

        // 3. Check nút "All" làm mặc định
        allChip.setChecked(true);
    }

    @Override
    public void onItemClick(Item item) {
        Bundle args = new Bundle();
        args.putString("itemId", item.getItemId());
        NavHostFragment.findNavController(this).navigate(R.id.action_global_to_itemDetailFragment, args);
    }

    @Override
    public void onFavoriteClick(Item item) {
        // TODO: Implement bookmark logic via ViewModel
        Toast.makeText(getContext(), "Favorite clicked on: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}