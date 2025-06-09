package com.example.tradeup.ui.profile

import android.app.Activity
import android.content.Intent // << THÊM IMPORT NÀY
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tradeup.R
import com.example.tradeup.data.model.ContactInfo
import com.example.tradeup.data.model.User
import com.example.tradeup.databinding.FragmentEditProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    companion object {
        private const val TAG = "EditProfileFragment"
    }

    // Sửa kiểu của ActivityResultLauncher thành Intent
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo launcher trong onCreate hoặc onViewCreated
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == Activity.RESULT_OK && data != null) {
                    selectedImageUri = data.data // data.data là Uri của ảnh
                    Log.d(TAG, "Image selected: $selectedImageUri")
                    binding.imageViewEditProfilePicture.setImageURI(selectedImageUri)
                } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "ImagePicker Error: ${ImagePicker.getError(data)}")
                } else {
                    Log.d(TAG, "Image selection cancelled by user")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        setupToolbar()
        setupClickListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbarEditProfile.setNavigationOnClickListener {
            Log.d(TAG, "Close button clicked")
            findNavController().popBackStack()
        }
    }

    private fun setupClickListeners() {
        binding.fabChangeProfilePicture.setOnClickListener {
            Log.d(TAG, "Change profile picture FAB clicked")
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent -> // Lambda này nhận một Intent
                    imagePickerLauncher.launch(intent) // Launch Intent đó
                }
        }

        binding.buttonSaveChanges.setOnClickListener {
            Log.d(TAG, "Save Changes button clicked")
            saveProfileChanges()
        }

        binding.buttonCancelChanges.setOnClickListener {
            Log.d(TAG, "Cancel Changes button clicked")
            findNavController().popBackStack()
        }
    }

    private fun populateUserDetails(user: User) {
        Log.d(TAG, "Populating user details: ${user.displayName}")
        Glide.with(this)
            .load(user.profilePictureUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.imageViewEditProfilePicture)

        binding.editTextEditDisplayName.setText(user.displayName)
        binding.editTextEditBio.setText(user.bio ?: "")
        binding.editTextEditPhoneNumber.setText(user.contactInfo?.phone ?: "")
        binding.editTextEditZalo.setText(user.contactInfo?.zalo ?: "")
        binding.editTextEditFacebook.setText(user.contactInfo?.facebook ?: "")
    }

    private fun saveProfileChanges() {
        val displayName = binding.editTextEditDisplayName.text.toString().trim()
        val bio = binding.editTextEditBio.text.toString().trim()
        val phone = binding.editTextEditPhoneNumber.text.toString().trim()
        val zalo = binding.editTextEditZalo.text.toString().trim()
        val facebook = binding.editTextEditFacebook.text.toString().trim()

        if (displayName.isEmpty()) {
            binding.tilEditDisplayName.error = "Tên hiển thị không được để trống"
            return
        } else {
            binding.tilEditDisplayName.error = null
        }

        val contactInfo = ContactInfo(
            phone = phone.ifEmpty { null },
            zalo = zalo.ifEmpty { null },
            facebook = facebook.ifEmpty { null }
        )

        Log.d(TAG, "Calling ViewModel updateUserProfile. Image URI: $selectedImageUri")
        profileViewModel.updateUserProfile(displayName, bio, contactInfo, selectedImageUri)
    }


    private fun setupObservers() {
        profileViewModel.currentUserForEdit.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Chỉ populate nếu selectedImageUri là null (tức là chưa chọn ảnh mới)
                // để tránh ghi đè ảnh mới chọn bằng ảnh cũ từ server khi LiveData update.
                if (selectedImageUri == null) {
                    populateUserDetails(user)
                }
            } else {
                Log.e(TAG, "Current user data is null, cannot populate edit form.")
                Toast.makeText(requireContext(), "Không thể tải dữ liệu người dùng.", Toast.LENGTH_LONG).show()
                binding.buttonSaveChanges.isEnabled = false
            }
        }

        profileViewModel.updateProfileState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "UpdateProfileState observed: $state")
            binding.progressBarEditProfile.isVisible = state is UpdateProfileState.Loading
            binding.buttonSaveChanges.isEnabled = state !is UpdateProfileState.Loading
            binding.buttonCancelChanges.isEnabled = state !is UpdateProfileState.Loading

            when (state) {
                is UpdateProfileState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Profile update success, navigating back.")
                    selectedImageUri = null // Reset ảnh đã chọn sau khi lưu thành công
                    findNavController().popBackStack()
                    profileViewModel.resetUpdateProfileState()
                }
                is UpdateProfileState.Error -> {
                    Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "UpdateProfileState.Error: ${state.message}")
                }
                UpdateProfileState.Idle -> {
                    Log.d(TAG, "UpdateProfileState.Idle")
                }
                UpdateProfileState.Loading -> {
                    Log.d(TAG, "UpdateProfileState.Loading")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileViewModel.resetUpdateProfileState()
        _binding = null
        Log.d(TAG, "onDestroyView called")
    }
}