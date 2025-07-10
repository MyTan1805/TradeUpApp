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
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentCategoryListingsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter; // << DÙNG PRODUCTADAPTER
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryListingsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentCategoryListingsBinding binding;
    private CategoryViewModel viewModel;
    private ProductAdapter adapter; // << DÙNG PRODUCTADAPTER

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
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Lấy title từ arguments để hiển thị ngay lập tức
        String categoryId = getArguments() != null ? getArguments().getString("categoryId") : null;
        if(categoryId != null) {
            binding.toolbar.setTitle("Category: " + categoryId);
        } else {
            binding.toolbar.setTitle("Recommended For You");
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewCategoryResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewCategoryResults.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
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

    @Override
    public void onItemClick(Item item) {
        Bundle args = new Bundle();
        args.putString("itemId", item.getItemId());
        NavHostFragment.findNavController(this).navigate(R.id.action_global_to_itemDetailFragment, args);
    }

    @Override
    public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {
        // TODO: Implement bookmark logic via ViewModel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}