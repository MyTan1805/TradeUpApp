package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentTabbedListBinding;
import com.example.tradeup.ui.adapters.MyListingAdapter;
import java.util.Collections;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SoldListingsFragment extends Fragment {

    private FragmentTabbedListBinding binding;
    private MyListingsViewModel viewModel;
    private MyListingAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(MyListingsViewModel.class);
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
        adapter = new MyListingAdapter(new MyListingAdapter.OnItemActionListener() {
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
                // Logic này đã được chuyển vào ViewModel, rất tốt!
                viewModel.onRateBuyerClicked(item);
            }

            @Override
            public void onItemClick(Item item) {
                if (isAdded() && item != null) {
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getItemId());
                    NavHostFragment.findNavController(SoldListingsFragment.this)
                            .navigate(R.id.action_global_to_itemDetailFragment, args);
                }
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // << FIX: Lắng nghe LiveData State tổng thể >>
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof MyListingsState.Success) {
                // Lấy danh sách soldItems từ State
                List<Item> soldItems = ((MyListingsState.Success) state).soldItems;
                adapter.submitList(soldItems);

                boolean isEmpty = (soldItems == null || soldItems.isEmpty());
                binding.textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.textViewEmpty.setText("You have no sold listings.");
            } else if (state instanceof MyListingsState.Loading) {
                // Xóa list cũ khi đang tải lại
                adapter.submitList(Collections.emptyList());
                binding.textViewEmpty.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}