// File: src/main/java/com/example/tradeup/ui/listing/MyListingsFragment.java

package com.example.tradeup.ui.listing;

import android.app.AlertDialog;
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

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentMyListingsBinding;
import com.example.tradeup.ui.adapters.MyListingsPagerAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel; // SỬA IMPORT
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyListingsFragment extends Fragment {

    private FragmentMyListingsBinding binding;
    private ProfileViewModel viewModel; // SỬA KIỂU DỮ LIỆU
    private MyListingsPagerAdapter pagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ProfileViewModel. Hilt sẽ cung cấp một instance mới cho màn hình này.
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPagerAndTabs();
        setupClickListeners();
        observeViewModel();
        setupFragmentResultListener();
    }

    private void setupClickListeners() {
        binding.fabAddNew.setOnClickListener(v -> {
            if (isAdded()) {
                // Giả sử có action này trong nav_graph
                NavHostFragment.findNavController(this).navigate(R.id.addItemFragment);
            }
        });
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBarMyListings.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(ListingOptionsDialogFragment.REQUEST_KEY, this, (requestKey, bundle) -> {
            String action = bundle.getString(ListingOptionsDialogFragment.KEY_ACTION);
            if (action == null) return;

            Item selectedItem = viewModel.getSelectedItem().getValue();
            if (selectedItem == null) {
                Toast.makeText(getContext(), "Error: No item was selected.", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (action) {
                case ListingOptionsDialogFragment.ACTION_EDIT:
                    Bundle args = new Bundle();
                    args.putString("itemId", selectedItem.getItemId());
                    NavHostFragment.findNavController(this).navigate(R.id.action_global_to_editItemFragment, args);
                    break;
                case ListingOptionsDialogFragment.ACTION_MARK_SOLD:
                    viewModel.updateSelectedItemStatus("sold");
                    break;
                case ListingOptionsDialogFragment.ACTION_PAUSE_RESUME:
                    String currentStatus = selectedItem.getStatus();
                    String newStatus = "paused".equalsIgnoreCase(currentStatus) ? "available" : "paused";
                    viewModel.updateSelectedItemStatus(newStatus);
                    break;
                case ListingOptionsDialogFragment.ACTION_DELETE:
                    showDeleteConfirmationDialog();
                    break;
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to permanently delete this listing? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteSelectedItem())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupViewPagerAndTabs() {
        pagerAdapter = new MyListingsPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Active");
                    break;
                case 1:
                    tab.setText("Sold");
                    break;
                case 2:
                    tab.setText("Paused");
                    break;
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}