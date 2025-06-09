package com.example.tradeup.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeup.data.model.ContactInfo
import com.example.tradeup.data.model.Item
import com.example.tradeup.data.model.User
import com.example.tradeup.data.repository.AuthRepository
import com.example.tradeup.data.repository.ItemRepository
import com.example.tradeup.data.repository.UserRepository
import com.example.tradeup.utils.uploadImageDirectlyToCloudinary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User, val items: List<Item>, val isCurrentUserProfile: Boolean) : ProfileState()
    data class Error(val message: String) : ProfileState()
    object Idle : ProfileState()
}
sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    data class Success(val message: String) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val itemRepository: ItemRepository, // << INJECT ITEM REPO
    private val savedStateHandle: SavedStateHandle // Để lấy arguments từ navigation
) : ViewModel() {

    private val _profileState = MutableLiveData<ProfileState>(ProfileState.Idle)
    val profileState: LiveData<ProfileState> = _profileState

    private val _updateProfileState = MutableLiveData<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: LiveData<UpdateProfileState> = _updateProfileState

    val currentAuthUserUid: String? = authRepository.getCurrentUser()?.uid

    // profileUserIdArg có thể được truyền từ navigation component
    // Nếu không có, mặc định sẽ là profile của người dùng hiện tại
    val profileUserIdArg: String? = savedStateHandle.get<String>("profileUserId")

    private val _currentUserForEdit = MutableLiveData<User?>()
    val currentUserForEdit: LiveData<User?> = _currentUserForEdit

    init {
        loadCurrentUserProfileForEditing()
        loadUserProfile()
    }
    fun loadCurrentUserProfileForEditing() {
        currentAuthUserUid?.let { uid ->
            // Không set _profileState thành Loading ở đây để tránh ảnh hưởng ProfileFragment
            viewModelScope.launch {
                userRepository.getUserProfile(uid).onSuccess { user ->
                    _currentUserForEdit.value = user
                }.onFailure {
                    _currentUserForEdit.value = null // Hoặc xử lý lỗi cụ thể
                    Log.e("ProfileViewModel", "Failed to load current user profile for editing: ${it.message}")
                }
            }
        }
    }

    fun loadUserProfile() {
        _profileState.value = ProfileState.Loading
        val targetUserId = profileUserIdArg ?: currentAuthUserUid

        if (targetUserId == null) {
            _profileState.value = ProfileState.Error("Không xác định được người dùng để hiển thị hồ sơ.")
            return
        }

        viewModelScope.launch {
            val userResult = userRepository.getUserProfile(targetUserId)
            val itemsResult = itemRepository.getItemsBySellerId(targetUserId) // Lấy sản phẩm của user

            userResult.onSuccess { user ->
                if (user != null) {
                    itemsResult.onSuccess { items ->
                        val isCurrentUser = targetUserId == currentAuthUserUid
                        _profileState.value = ProfileState.Success(user, items, isCurrentUser)
                    }.onFailure { itemException ->
                        // Vẫn hiển thị user info nếu lấy item lỗi
                        val isCurrentUser = targetUserId == currentAuthUserUid
                        _profileState.value = ProfileState.Success(user, emptyList(), isCurrentUser)
                        // Có thể log lỗi lấy item ở đây
                        Log.e("ProfileViewModel", "Failed to load user items: ${itemException.message}")
                    }
                } else {
                    _profileState.value = ProfileState.Error("Không tìm thấy thông tin người dùng.")
                }
            }.onFailure { userException ->
                _profileState.value = ProfileState.Error("Lỗi tải thông tin người dùng: ${userException.message}")
            }
        }
    }

    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }
    fun updateUserProfile(
        updatedDisplayName: String,
        updatedBio: String?,
        updatedContactInfo: ContactInfo, // Giả sử User.ContactInfo là data class
        newProfileImageUri: Uri? // Uri của ảnh mới nếu người dùng chọn
    ) {
        _updateProfileState.value = UpdateProfileState.Loading
        val currentUser = _currentUserForEdit.value // Lấy user hiện tại đã load
        val uid = currentAuthUserUid

        if (uid == null || currentUser == null) {
            _updateProfileState.value = UpdateProfileState.Error("Không thể cập nhật, người dùng không xác định.")
            return
        }

        viewModelScope.launch {
            var newImageUrl: String? = currentUser.profilePictureUrl // Giữ ảnh cũ nếu không có ảnh mới

            if (newProfileImageUri != null) {
                // Upload ảnh mới lên Cloudinary
                val uploadResult = uploadImageDirectlyToCloudinary(applicationContext, newProfileImageUri)
                if (uploadResult != null) {
                    newImageUrl = uploadResult
                    Log.d("ProfileViewModel", "New image uploaded to Cloudinary: $newImageUrl")
                } else {
                    _updateProfileState.value = UpdateProfileState.Error("Lỗi tải ảnh lên. Vui lòng thử lại.")
                    Log.e("ProfileViewModel", "Cloudinary upload failed.")
                    return@launch
                }
            }

            // Tạo đối tượng User mới với thông tin đã cập nhật
            val updatedUser = currentUser.copy(
                displayName = updatedDisplayName,
                bio = updatedBio ?: currentUser.bio, // Giữ bio cũ nếu updatedBio là null
                profilePictureUrl = newImageUrl,
                contactInfo = updatedContactInfo ?: currentUser.contactInfo, // Giữ contact cũ nếu updatedContactInfo là null
                updatedAt = null // Để @ServerTimestamp tự động cập nhật khi ghi vào Firestore
            )

            val result = userRepository.updateUserProfile(updatedUser)
            result.onSuccess {
                _updateProfileState.value = UpdateProfileState.Success("Hồ sơ đã được cập nhật thành công!")
                // Load lại profile để cập nhật _currentUserForEdit và _profileState
                loadCurrentUserProfileForEditing()
                loadUserProfile() // Load lại profile chung nếu đang ở ProfileFragment
                Log.d("ProfileViewModel", "Profile updated successfully in Firestore.")
            }.onFailure { exception ->
                _updateProfileState.value = UpdateProfileState.Error("Lỗi cập nhật hồ sơ: ${exception.message}")
                Log.e("ProfileViewModel", "Firestore profile update failed: ${exception.message}")
            }
        }
    }

    fun logout() {
        // Gọi hàm logout từ AuthRepository hoặc AuthViewModel nếu cần quản lý trạng thái chung
        // Ví dụ, nếu AuthViewModel quản lý _authState cho logout:
        // authViewModel.logoutUser() // Cần inject AuthViewModel hoặc có cách gọi khác
        // Hoặc gọi trực tiếp repository:
        authRepository.logoutUser()
        // Có thể cần cập nhật một LiveData khác để Fragment biết đã logout và điều hướng
    }

    fun refreshProfile() {
        loadUserProfile()
    }
}