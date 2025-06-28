// File: src/main/java/com/example/tradeup/ui/profile/EditProfileFragment.java
package com.example.tradeup.ui.profile;

import android.app.Activity;
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
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentEditProfileBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private EditProfileViewModel viewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // Khởi tạo launcher để chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // Hiển thị ảnh mới được chọn và báo cho ViewModel
                            Glide.with(this).load(imageUri).into(binding.imageViewProfilePicture);
                            viewModel.setNewProfilePicture(imageUri);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        // Nút quay lại và hủy đều có cùng chức năng
        binding.buttonBack.setOnClickListener(v -> navigateBack());
        binding.buttonCancel.setOnClickListener(v -> navigateBack());

        // Mở thư viện ảnh khi nhấn vào ảnh hoặc nút đổi ảnh
        View.OnClickListener openImagePicker = v -> openImagePicker();
        binding.imageViewProfilePicture.setOnClickListener(openImagePicker);
        binding.fabChangeProfilePicture.setOnClickListener(openImagePicker);
        binding.textChangeProfilePicture.setOnClickListener(openImagePicker);

        // Nút lưu thay đổi
        binding.buttonSaveChanges.setOnClickListener(v -> {
            String displayName = binding.editTextDisplayName.getText().toString().trim();
            String bio = binding.editTextBio.getText().toString().trim();
            String phone = binding.editTextPhoneNumber.getText().toString().trim();
            viewModel.saveChanges(displayName, bio, phone);
        });
    }

    private void observeViewModel() {
        // Lắng nghe dữ liệu người dùng ban đầu
        viewModel.getUser().observe(getViewLifecycleOwner(), this::populateUiWithUserData);

        // Lắng nghe trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonSaveChanges.setEnabled(!isLoading);
            binding.buttonSaveChanges.setText(isLoading ? "Saving..." : "Save Changes");
            // Có thể thêm ProgressBar ở đây
        });

        // Lắng nghe sự kiện lỗi
        viewModel.getErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String errorMessage = event.getContentIfNotHandled();
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe sự kiện lưu thành công
        viewModel.getSaveSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            Boolean isSuccess = event.getContentIfNotHandled();
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    private void populateUiWithUserData(User user) {
        if (user == null) return;

        // Tải ảnh đại diện
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        // Điền thông tin vào các trường
        binding.editTextDisplayName.setText(user.getDisplayName());
        binding.editTextBio.setText(user.getBio());

        // Giả sử số điện thoại nằm trong ContactInfo
        if (user.getContactInfo() != null) {
            binding.editTextPhoneNumber.setText(user.getContactInfo().getPhone());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void navigateBack() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}