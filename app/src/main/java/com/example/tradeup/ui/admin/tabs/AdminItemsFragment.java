// File: src/main/java/com/example/tradeup/ui/admin/tabs/AdminItemsFragment.java
package com.example.tradeup.ui.admin.tabs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentAdminItemsBinding;
import com.example.tradeup.ui.admin.AdminItemAdapter;
import com.example.tradeup.ui.admin.AdminViewModel;

public class AdminItemsFragment extends Fragment {

    private FragmentAdminItemsBinding binding;
    private AdminViewModel viewModel;
    private AdminItemAdapter adapter;
    private NavController navController;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(AdminViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminItemsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        setupRecyclerView();
        setupSearchView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AdminItemAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("itemId", item.getItemId());
            navController.navigate(R.id.action_global_to_itemDetailFragment, args);
        });
        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewItems.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.editTextSearchItems.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Sử dụng "debounce" để không gọi API mỗi khi gõ một ký tự
                searchHandler.removeCallbacks(searchRunnable);
                String query = s.toString();
                searchRunnable = () -> viewModel.searchItems(query);
                searchHandler.postDelayed(searchRunnable, 500); // Trì hoãn 500ms
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.isItemSearchLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarItems.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getItemSearchResults().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean hasResults = items != null && !items.isEmpty();
            boolean isQueryEmpty = binding.editTextSearchItems.getText().toString().isEmpty();

            // *** SỬA LẠI LOGIC HIỂN THỊ EMPTY STATE ***
            if (isQueryEmpty) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.imageViewEmptyIcon.setImageResource(R.drawable.ic_search);
                binding.textViewEmptyItems.setText("Start typing to search for items.");
                binding.recyclerViewItems.setVisibility(View.GONE);
            } else {
                if (hasResults) {
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewItems.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.imageViewEmptyIcon.setImageResource(R.drawable.ic_search_off);
                    binding.textViewEmptyItems.setText("No items found.");
                    binding.recyclerViewItems.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}