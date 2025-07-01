// File: src/main/java/com/example/tradeup/ui/listing/EditItemFragment.java

package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentEditItemBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditItemFragment extends Fragment {

    private FragmentEditItemBinding binding;
    private EditItemViewModel viewModel;
    // TODO: Khai báo và khởi tạo adapter cho ảnh sau

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditItemViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Sử dụng ID chính xác từ layout: toolbarEdit
        binding.toolbarEdit.setNavigationOnClickListener(v -> navigateBack());

        binding.buttonSaveChanges.setOnClickListener(v -> {
            if (validateInput()) {
                String title = binding.editTextTitle.getText().toString().trim();
                String description = binding.editTextDescription.getText().toString().trim();
                String price = binding.editTextPrice.getText().toString().trim();
                viewModel.saveChanges(title, description, price);
            }
        });

        // TODO: Thêm listener cho các trường có thể sửa khác (Condition, Location)
    }

    private void observeViewModel() {
        // Lắng nghe dữ liệu item ban đầu để điền vào UI
        viewModel.getItem().observe(getViewLifecycleOwner(), this::populateUi);

        // Lắng nghe trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.buttonSaveChanges.setEnabled(!isLoading);
            binding.buttonSaveChanges.setText(isLoading ? "Saving..." : "Save Changes");
            // Sử dụng ID chính xác từ layout: progressBarEdit
            binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Lắng nghe sự kiện lỗi
        viewModel.getErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe sự kiện lưu thành công
        viewModel.getUpdateSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            Boolean isSuccess = event.getContentIfNotHandled();
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Listing updated successfully!", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    private void populateUi(Item item) {
        if (item == null || binding == null) return;

        binding.editTextTitle.setText(item.getTitle());
        binding.editTextDescription.setText(item.getDescription());
        binding.editTextPrice.setText(String.valueOf(item.getPrice()));

        // Điền dữ liệu cho các trường chỉ đọc
        // TODO: Cần có AppConfig để lấy tên từ ID
        binding.fieldCategory.setText(item.getCategory());
        binding.fieldCondition.setText(item.getCondition());
        if (item.getLocation() != null) {
            binding.fieldLocation.setText(item.getLocation().getAddressString());
        }

        // TODO: Load ảnh hiện có vào RecyclerView
        // Ví dụ: photoAdapter.setImages(item.getImageUrls());
    }

    private boolean validateInput() {
        if (binding == null) return false;

        boolean isValid = true;

        if (TextUtils.isEmpty(binding.editTextTitle.getText())) {
            binding.tilTitle.setError("Title cannot be empty");
            isValid = false;
        } else {
            binding.tilTitle.setError(null);
        }

        if (TextUtils.isEmpty(binding.editTextPrice.getText())) {
            binding.tilPrice.setError("Price cannot be empty");
            isValid = false;
        } else {
            binding.tilPrice.setError(null);
        }

        return isValid;
    }

    private void navigateBack() {
        if(isAdded()) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}