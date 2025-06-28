// File: src/main/java/com/example/tradeup/ui/details/ItemDetailFragment.java
package com.example.tradeup.ui.details;

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
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentItemDetailBinding;
import com.example.tradeup.ui.adapters.ImageSliderAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.NumberFormat;
import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ItemDetailFragment extends Fragment {

    private FragmentItemDetailBinding binding;
    private ItemDetailViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ItemDetailViewModel.class);
        // ViewModel sẽ tự động tải dữ liệu trong constructor của nó bằng SavedStateHandle
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        observeViewModel(); // Chỉ cần gọi hàm này
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.toolbar.setOnMenuItemClickListener(item -> {
            // === FIX: ID của menu item được lấy đúng từ R.id ===
            int menuItemId = item.getItemId();
            if (menuItemId == R.id.action_share) {
                Toast.makeText(getContext(), "Share clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (menuItemId == R.id.action_bookmark) {
                Toast.makeText(getContext(), "Bookmark clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        // === FIX: Quan sát một LiveData duy nhất là getViewState() ===
        viewModel.getViewState().observe(getViewLifecycleOwner(), state -> {
            // Ẩn tất cả các view trạng thái trước
            binding.progressBar.setVisibility(View.GONE); // Giả sử bạn có ProgressBar với id này

            if (state instanceof ItemDetailViewState.Loading) {
                // Hiển thị trạng thái đang tải
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof ItemDetailViewState.Success) {
                // Lấy dữ liệu từ trạng thái Success
                ItemDetailViewState.Success successState = (ItemDetailViewState.Success) state;
                // Gọi hàm populateUi với dữ liệu đã được xử lý
                populateUi(successState.item, successState.categoryName, successState.conditionName);
            } else if (state instanceof ItemDetailViewState.Error) {
                // Hiển thị lỗi
                Toast.makeText(getContext(), ((ItemDetailViewState.Error) state).message, Toast.LENGTH_LONG).show();
                // Quay lại màn hình trước nếu có lỗi nghiêm trọng
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    // === FIX: Sửa lại tham số của hàm này ===
    private void populateUi(Item item, String categoryName, String conditionName) {
        // Setup ViewPager2
        ImageSliderAdapter adapter = new ImageSliderAdapter(item.getImageUrls());
        binding.viewPagerImages.setAdapter(adapter);

        // Kết nối ViewPager2 với TabLayout để tạo indicator
        new TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerImages, (tab, position) -> {}).attach();

        // Điền thông tin
        binding.textItemTitle.setText(item.getTitle());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        binding.textItemPrice.setText(currencyFormat.format(item.getPrice()));

        // Hiển thị tên tình trạng đã được xử lý từ ViewModel
        binding.textItemCondition.setText(conditionName);

        // Thông tin người bán
        Glide.with(this).load(item.getSellerProfilePictureUrl()).into(binding.imageSellerAvatar);
        binding.textSellerName.setText(item.getSellerDisplayName());

        // Mô tả và chi tiết
        binding.textItemDescription.setText(item.getDescription());

        // TODO: Cập nhật các trường khác như Brand, Color, Dimensions từ model Item nếu có
        // binding.textDetailBrand.setText(item.getBrand());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}