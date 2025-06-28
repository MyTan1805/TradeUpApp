// File: src/main/java/com/example/tradeup/ui/profile/tabs/ProfileListingsFragment.java
package com.example.tradeup.ui.profile.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentProfileListingsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileListingsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentProfileListingsBinding binding;
    private ProfileViewModel sharedViewModel;
    private ProductAdapter productAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewListings.setAdapter(productAdapter);
    }

    private void setupObservers() {
        sharedViewModel.getActiveListings().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                productAdapter.submitList(items);
                binding.textViewEmptyListings.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onItemClick(Item item) {
        Toast.makeText(getContext(), "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}