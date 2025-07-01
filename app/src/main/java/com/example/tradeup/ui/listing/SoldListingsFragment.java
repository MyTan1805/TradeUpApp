// File: src/main/java/com/example/tradeup/ui/listing/SoldListingsFragment.java

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentTabbedListBinding;
import com.example.tradeup.ui.adapters.MyListingAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SoldListingsFragment extends Fragment {

    private FragmentTabbedListBinding binding;
    private ProfileViewModel viewModel;
    private MyListingAdapter adapter;

    private List<Transaction> transactionList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // Sản phẩm đã bán có thể có các tùy chọn khác, ví dụ "Archive"
                viewModel.setSelectedItem(item);
                if (isAdded()) {
                    ListingOptionsDialogFragment.newInstance()
                            .show(getParentFragmentManager(), ListingOptionsDialogFragment.TAG);
                }
            }

            // Trong setupRecyclerView của SoldListingsFragment.java
            @Override
            public void onRateBuyerClick(Item item) {
                if (item.getSoldToUserId() == null) {
                    Toast.makeText(getContext(), "Buyer info not available.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tìm transaction tương ứng
                Transaction correspondingTransaction = null;
                for (Transaction t : transactionList) {
                    if (t.getItemId().equals(item.getItemId())) {
                        correspondingTransaction = t;
                        break;
                    }
                }

                if (correspondingTransaction == null) {
                    Toast.makeText(getContext(), "Transaction record not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Bây giờ chúng ta có đủ thông tin
                Bundle args = new Bundle();
                args.putString("transactionId", correspondingTransaction.getTransactionId());
                args.putString("ratedUserId", item.getSoldToUserId());
                args.putString("itemId", item.getItemId());

                if (isAdded()) {
                    NavHostFragment.findNavController(SoldListingsFragment.this)
                            .navigate(R.id.action_global_to_submitReviewFragment, args);
                }
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Lắng nghe danh sách sản phẩm "Sold" từ ViewModel
        viewModel.getSoldItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.submitList(items);
                binding.textViewEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                binding.textViewEmpty.setText("You have no sold listings.");
            }
        });

        viewModel.getSoldTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                this.transactionList = transactions;
            }
        });
    }

    // Tạo một lớp mới trong package model
    public class SoldItemWrapper {
        public Item item;
        public Transaction transaction;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}