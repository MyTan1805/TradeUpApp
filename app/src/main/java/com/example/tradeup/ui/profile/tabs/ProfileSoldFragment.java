// File: src/main/java/com/example/tradeup/ui/profile/tabs/ProfileSoldFragment.java
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
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentProfileSoldBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileSoldFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentProfileSoldBinding binding;
    private ProfileViewModel sharedViewModel;
    private ProductAdapter productAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel được chia sẻ từ parent fragment (ProfileFragment)
        sharedViewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileSoldBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        // Giả sử bạn có một ProductAdapter đã được định nghĩa
        productAdapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, this);
        binding.recyclerViewSold.setAdapter(productAdapter);
    }

    private void setupObservers() {
        sharedViewModel.getSoldTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                // Chuyển đổi List<Transaction> thành List<Item> để adapter hiển thị
                List<Item> soldItems = transactions.stream()
                        .map(this::convertTransactionToItem)
                        .collect(Collectors.toList());

                productAdapter.submitList(soldItems);
                binding.textViewEmptySold.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Hàm trợ giúp chuyển đổi một đối tượng Transaction thành một đối tượng Item tối giản.
     * @param transaction Giao dịch cần chuyển đổi.
     * @return Một đối tượng Item với các thông tin cơ bản để hiển thị.
     */
    private Item convertTransactionToItem(Transaction transaction) {
        Item item = new Item();
        // === SỬA TÊN HÀM: setItemId -> setId (để khớp với Item.java) ===
        item.setItemId(transaction.getItemId());
        item.setTitle(transaction.getItemTitle());
        item.setPrice(transaction.getPriceSold());

        // Tạo một danh sách ảnh chỉ chứa một ảnh bìa từ transaction
        if (transaction.getItemImageUrl() != null && !transaction.getItemImageUrl().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add(transaction.getItemImageUrl());
            // Gọi đúng phương thức setImageUrls từ Item.java
            item.setImageUrls(imageUrls);
        }

        return item;
    }


    @Override
    public void onItemClick(Item item) {
        Toast.makeText(getContext(), "View transaction for: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Item item, boolean isCurrentlyFavorite) {
        // Không áp dụng cho các sản phẩm đã bán
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}