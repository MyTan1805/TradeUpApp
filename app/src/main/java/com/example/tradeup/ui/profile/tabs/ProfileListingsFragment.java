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
import com.example.tradeup.databinding.FragmentProfileListingsBinding;
import com.example.tradeup.ui.adapters.ProductAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;

public class ProfileListingsFragment extends Fragment {

    private FragmentProfileListingsBinding binding;
    private ProfileViewModel viewModel;
    private ProductAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ fragment cha (ProfileFragment)
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);

        // Khởi tạo Adapter ở đây, trước khi View được tạo
        adapter = new ProductAdapter(ProductAdapter.VIEW_TYPE_GRID, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onItemClick(Item item) {
                // Xử lý khi người dùng nhấn vào một sản phẩm
                if (isAdded() && item != null) {
                    Bundle args = new Bundle();
                    args.putString("itemId", item.getItemId());
                    // Dùng action toàn cục để điều hướng đến màn hình chi tiết
                    NavHostFragment.findNavController(ProfileListingsFragment.this)
                            .navigate(R.id.action_global_to_itemDetailFragment, args);
                }
            }

            @Override
            public void onFavoriteClick(Item item) {
                Toast.makeText(getContext(), "Favorite clicked on: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileListingsBinding.inflate(inflater, container, false);

        // Gán LayoutManager và Adapter ngay lập tức sau khi có View
        // Điều này sẽ loại bỏ cảnh báo "No adapter attached"
        binding.recyclerViewListings.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewListings.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Bây giờ chỉ cần lắng nghe dữ liệu
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getActiveListings().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                // Cập nhật dữ liệu cho adapter
                adapter.submitList(items);
                // Hiển thị/ẩn thông báo rỗng
                binding.textViewEmptyListings.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Quan trọng: Gỡ bỏ tham chiếu đến binding để tránh memory leak
        binding = null;
    }
}