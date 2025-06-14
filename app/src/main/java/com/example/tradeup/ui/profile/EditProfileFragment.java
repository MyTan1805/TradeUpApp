package com.example.tradeup.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
// import androidx.core.view.ViewKt; // Không dùng nữa
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.ContactInfo;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentEditProfileBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.util.Objects; // Thêm import này

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private Uri selectedImageUri = null;

    private static final String TAG = "EditProfileFragment";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        Log.d(TAG, "onCreate called");

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK && data != null) {
                        selectedImageUri = data.getData();
                        if (selectedImageUri != null && binding != null) {
                            Log.d(TAG, "Image selected: " + selectedImageUri.toString());
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(binding.imageViewEditProfilePicture);
                        }
                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "ImagePicker Error: " + ImagePicker.getError(data));
                    } else {
                        Log.d(TAG, "Image selection cancelled by user");
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView called");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        setupToolbar();
        setupClickListeners();
        setupObservers();

        profileViewModel.fetchCurrentUserForEditing();
    }

    private void setupToolbar() {
        binding.toolbarEditProfile.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Close button clicked");
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    private void setupClickListeners() {
        binding.fabChangeProfilePicture.setOnClickListener(v -> {
            Log.d(TAG, "Change profile picture FAB clicked");
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });

        binding.buttonSaveChanges.setOnClickListener(v -> {
            Log.d(TAG, "Save Changes button clicked");
            saveProfileChanges();
        });

        binding.buttonCancelChanges.setOnClickListener(v -> {
            Log.d(TAG, "Cancel Changes button clicked");
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    private void populateUserDetails(User user) {
        if (user == null || binding == null) return;
        Log.d(TAG, "Populating user details: " + user.getDisplayName());

        if (selectedImageUri == null) { // Chỉ load từ URL nếu chưa chọn ảnh mới
            Glide.with(this)
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.imageViewEditProfilePicture);
        }


        if (binding.editTextEditDisplayName.getText() != null) {
            binding.editTextEditDisplayName.setText(user.getDisplayName());
        }
        if (binding.editTextEditBio.getText() != null) {
            binding.editTextEditBio.setText(user.getBio() != null ? user.getBio() : "");
        }

        ContactInfo contactInfo = user.getContactInfo();
        if (contactInfo != null) {
            if (binding.editTextEditPhoneNumber.getText() != null) {
                binding.editTextEditPhoneNumber.setText(contactInfo.getPhone() != null ? contactInfo.getPhone() : "");
            }
            if (binding.editTextEditZalo.getText() != null) {
                binding.editTextEditZalo.setText(contactInfo.getZalo() != null ? contactInfo.getZalo() : "");
            }
            if (binding.editTextEditFacebook.getText() != null) {
                binding.editTextEditFacebook.setText(contactInfo.getFacebook() != null ? contactInfo.getFacebook() : "");
            }
        } else {
            if (binding.editTextEditPhoneNumber.getText() != null) binding.editTextEditPhoneNumber.setText("");
            if (binding.editTextEditZalo.getText() != null) binding.editTextEditZalo.setText("");
            if (binding.editTextEditFacebook.getText() != null) binding.editTextEditFacebook.setText("");
        }
    }

    private void saveProfileChanges() {
        String displayName = binding.editTextEditDisplayName.getText() != null ? binding.editTextEditDisplayName.getText().toString().trim() : "";
        String bio = binding.editTextEditBio.getText() != null ? binding.editTextEditBio.getText().toString().trim() : null; // Cho phép bio là null
        String phone = binding.editTextEditPhoneNumber.getText() != null ? binding.editTextEditPhoneNumber.getText().toString().trim() : "";
        String zalo = binding.editTextEditZalo.getText() != null ? binding.editTextEditZalo.getText().toString().trim() : "";
        String facebook = binding.editTextEditFacebook.getText() != null ? binding.editTextEditFacebook.getText().toString().trim() : "";

        if (displayName.isEmpty()) {
            binding.tilEditDisplayName.setError(getString(R.string.error_ten_hien_thi_trong));
            return;
        } else {
            binding.tilEditDisplayName.setError(null);
        }

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhone(TextUtils.isEmpty(phone) ? null : phone);
        contactInfo.setZalo(TextUtils.isEmpty(zalo) ? null : zalo);
        contactInfo.setFacebook(TextUtils.isEmpty(facebook) ? null : facebook);

        Log.d(TAG, "Calling ViewModel updateUserProfile. Image URI: " + (selectedImageUri != null ? selectedImageUri.toString() : "null"));
        profileViewModel.updateUserProfile(displayName, bio, contactInfo, selectedImageUri);
    }


    private void setupObservers() {
        // Sử dụng getter từ ProfileViewModel.java
        profileViewModel.getCurrentUserForEdit().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (selectedImageUri == null) { // Chỉ populate nếu chưa có ảnh mới được chọn
                    populateUserDetails(user);
                }
            } else {
                Log.e(TAG, "Current user data for edit is null, cannot populate edit form.");
                if (binding != null) binding.buttonSaveChanges.setEnabled(false);
                // Cân nhắc hiển thị Toast hoặc thông báo lỗi ở đây nếu cần
            }
        });

        profileViewModel.getUpdateProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            Log.d(TAG, "UpdateProfileState observed: " + state.getClass().getSimpleName());

            binding.progressBarEditProfile.setVisibility(state instanceof UpdateProfileState.Loading ? View.VISIBLE : View.GONE);
            binding.buttonSaveChanges.setEnabled(!(state instanceof UpdateProfileState.Loading));
            binding.buttonCancelChanges.setEnabled(!(state instanceof UpdateProfileState.Loading));

            if (state instanceof UpdateProfileState.Success) {
                UpdateProfileState.Success successState = (UpdateProfileState.Success) state;
                Toast.makeText(requireContext(), successState.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Profile update success, navigating back.");
                selectedImageUri = null;
                if (isAdded()) {
                    NavHostFragment.findNavController(this).popBackStack();
                }
                profileViewModel.resetUpdateProfileState();
            } else if (state instanceof UpdateProfileState.Error) {
                UpdateProfileState.Error errorState = (UpdateProfileState.Error) state;
                Toast.makeText(requireContext(), "Lỗi: " + errorState.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "UpdateProfileState.Error: " + errorState.getMessage());
            } else if (state instanceof UpdateProfileState.Idle) {
                Log.d(TAG, "UpdateProfileState.Idle");
            } else if (state instanceof UpdateProfileState.Loading) {
                Log.d(TAG, "UpdateProfileState.Loading");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        profileViewModel.resetUpdateProfileState();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}