// File: src/main/java/com/example/tradeup/ui/listing/ActiveListingsFragment.java

package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentTabbedListBinding;
import com.example.tradeup.ui.adapters.MyListingAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActiveListingsFragment extends Fragment {

    private FragmentTabbedListBinding binding;
    private ProfileViewModel viewModel;
    private MyListingAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy instance của ViewModel từ Fragment cha (MyListingsFragment)
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTabbedListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new MyListingAdapter(new MyListingAdapter.OnItemMenuClickListener() {
            @Override
            public void onMenuClick(Item item) {
                viewModel.setSelectedItem(item);
                if (isAdded()) {
                    ListingOptionsDialogFragment.newInstance()
                            .show(getParentFragmentManager(), ListingOptionsDialogFragment.TAG);
                }
            }

            @Override
            public void onRateBuyerClick(Item item) {
                // Không có hành động gì trong tab Active
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Lắng nghe danh sách sản phẩm "Active" từ ViewModel
        viewModel.getActiveListings().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.submitList(items);
                binding.textViewEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                binding.textViewEmpty.setText("You have no active listings.");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}