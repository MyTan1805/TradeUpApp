// package: com.example.tradeup.ui.listing
package com.example.tradeup.ui.listing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item; // Import model Item
import com.example.tradeup.databinding.FragmentAddItemBinding;
import com.example.tradeup.ui.adapters.AddItemImageAdapter;
import com.github.dhaval2404.imagepicker.ImagePicker;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddItemFragment extends Fragment implements AddItemImageAdapter.OnImageActionsListener {

    private FragmentAddItemBinding binding;
    private AddItemViewModel viewModel;
    private AddItemImageAdapter imageAdapter;
    private ProgressDialog progressDialog;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private List<Uri> selectedImageUris = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddItemViewModel.class);

        // Khởi tạo launcher cho ImagePicker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Thêm ảnh vào danh sách và cập nhật adapter
                            selectedImageUris.add(uri);
                            imageAdapter.setImageUris(selectedImageUris);
                        }
                    } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(getContext(), ImagePicker.getError(result.getData()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupUI() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        binding.buttonCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.buttonPostListing.setOnClickListener(v -> handlePostListing());
    }

    private void setupRecyclerView() {
        imageAdapter = new AddItemImageAdapter(this);
        binding.recyclerViewImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewImages.setAdapter(imageAdapter);
        imageAdapter.setImageUris(selectedImageUris); // Hiển thị danh sách ban đầu
    }

    private void observeViewModel() {
        viewModel.getAddItemState().observe(getViewLifecycleOwner(), state -> {
            // Xử lý các trạng thái khác nhau từ ViewModel
            if (state instanceof AddItemState.Idle) {
                progressDialog.dismiss();
            } else if (state instanceof AddItemState.Loading) {
                progressDialog.setMessage(((AddItemState.Loading) state).message);
                progressDialog.show();
            } else if (state instanceof AddItemState.Success) {
                progressDialog.dismiss();
                String itemId = ((AddItemState.Success) state).itemId;
                Toast.makeText(getContext(), "Đăng tin thành công!", Toast.LENGTH_LONG).show();
                // TODO: Điều hướng đến trang chi tiết sản phẩm vừa đăng với itemId
                // ví dụ: AddItemFragmentDirections.ActionAddItemToItemDetail action = ...;
                // navController.navigate(action);
                NavHostFragment.findNavController(this).navigateUp(); // Tạm thời quay lại
            } else if (state instanceof AddItemState.Error) {
                progressDialog.dismiss();
                String errorMessage = ((AddItemState.Error) state).message;
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handlePostListing() {
        // 1. Thu thập dữ liệu từ UI
        String title = binding.editTextTitle.getText().toString().trim();
        String priceStr = binding.editTextPrice.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();

        // TODO: Lấy giá trị thực từ các trường Category, Condition, Location đã chọn
        String category = "electronics"; // Ví dụ
        String condition = "used_like_new"; // Ví dụ

        // 2. Validate dữ liệu cơ bản
        if (title.isEmpty() || priceStr.isEmpty() || selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền tiêu đề, giá và chọn ít nhất một ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Tạo đối tượng Item
        Item itemToPost = new Item();
        itemToPost.setTitle(title);
        try {
            itemToPost.setPrice(Double.parseDouble(priceStr));
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }
        itemToPost.setDescription(description);
        itemToPost.setCategory(category);
        itemToPost.setCondition(condition);
        itemToPost.setStatus("available");
        // ... set các trường khác như location...

        // 4. Gọi phương thức của ViewModel để bắt đầu quá trình đăng tin
        viewModel.postItem(itemToPost, selectedImageUris);
    }

    @Override
    public void onAddImageClick() {
        ImagePicker.with(this)
                .compress(1024) // Giảm kích thước file (KB)
                .maxResultSize(1080, 1080) // Giảm độ phân giải ảnh
                .createIntent(intent -> {
                    imagePickerLauncher.launch(intent);
                    return null;
                });
    }

    @Override
    public void onRemoveImageClick(int position) {
        if (position >= 0 && position < selectedImageUris.size()) {
            selectedImageUris.remove(position);
            imageAdapter.setImageUris(selectedImageUris); // Cập nhật lại adapter
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}