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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentProfileSoldBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProfileSoldFragment extends Fragment {

    private FragmentProfileSoldBinding binding;
    private ProfileViewModel viewModel;
    private ProductAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);

        // Khởi tạo adapter trong onCreate
        adapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onItemClick(Item item) {
                if (isAdded() && item != null) {
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getItemId());
                    NavHostFragment.findNavController(ProfileSoldFragment.this)
                            .navigate(R.id.action_global_to_itemDetailFragment, args);
                }
            }

            @Override
            public void onFavoriteClick(Item item) {
                // Không làm gì cho sản phẩm đã bán
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileSoldBinding.inflate(inflater, container, false);

        // Gán LayoutManager và Adapter ngay lập tức
        binding.recyclerViewSold.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewSold.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getSoldTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                // Chuyển đổi từ List<Transaction> sang List<Item> để adapter hiển thị
                List<Item> soldItems = new ArrayList<>();
                for (Transaction t : transactions) {
                    Item item = new Item();
                    item.setItemId(t.getItemId());
                    item.setTitle(t.getItemTitle());
                    if (t.getItemImageUrl() != null) {
                        item.getImageUrls().add(t.getItemImageUrl());
                    }
                    item.setPrice(t.getPriceSold());
                    item.setStatus("sold");
                    soldItems.add(item);
                }
                adapter.submitList(soldItems);
                binding.textViewEmptySold.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}