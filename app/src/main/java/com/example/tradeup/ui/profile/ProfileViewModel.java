package com.example.tradeup.ui.profile;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import com.example.tradeup.data.model.ContactInfo;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository; // Java interface
import com.example.tradeup.data.repository.ItemRepository;   // Java interface
import com.example.tradeup.data.repository.UserRepository;   // Java interface
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";

    private final Context applicationContext;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    // private final SavedStateHandle savedStateHandle; // Không cần gán lại nếu đã là final và inject

    private final MutableLiveData<ProfileState> _profileState = new MutableLiveData<>(new ProfileState.Idle());
    public LiveData<ProfileState> getProfileState() { return _profileState; }

    private final MutableLiveData<UpdateProfileState> _updateProfileState = new MutableLiveData<>(new UpdateProfileState.Idle());
    public LiveData<UpdateProfileState> getUpdateProfileState() { return _updateProfileState; }

    private final String currentAuthUserUid;
    private final String profileUserIdArg;

    private final MutableLiveData<User> _currentUserForEdit = new MutableLiveData<>();
    public LiveData<User> getCurrentUserForEdit() { return _currentUserForEdit; }

    @Inject
    public ProfileViewModel(
            @ApplicationContext Context applicationContext,
            AuthRepository authRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            SavedStateHandle savedStateHandle
    ) {
        this.applicationContext = applicationContext;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        // this.savedStateHandle = savedStateHandle;

        FirebaseUser fbUser = authRepository.getCurrentUser();
        this.currentAuthUserUid = (fbUser != null) ? fbUser.getUid() : null;
        this.profileUserIdArg = savedStateHandle.get("profileUserId");

        Log.d(TAG, "ViewModel Init - CurrentAuthUID: " + currentAuthUserUid + ", ProfileArgUID: " + profileUserIdArg);

        fetchCurrentUserForEditing(); // Tải profile của user hiện tại cho màn hình edit
        loadUserProfile(); // Tải profile dựa trên argument hoặc user hiện tại cho màn hình view profile
    }

    @Nullable
    public String getCurrentAuthUserUid() {
        return currentAuthUserUid;
    }

    @Nullable
    public String getProfileUserIdArg() {
        return profileUserIdArg;
    }

    public void fetchCurrentUserForEditing() {
        if (currentAuthUserUid != null) {
            userRepository.getUserProfile(currentAuthUserUid, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    _currentUserForEdit.postValue(user);
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    _currentUserForEdit.postValue(null);
                    Log.e(TAG, "EDIT_PROFILE: Failed to load current user profile: " + e.getMessage());
                }
            });
        } else {
            _currentUserForEdit.postValue(null);
            Log.w(TAG, "EDIT_PROFILE: currentAuthUserUid is null, cannot load profile for editing.");
        }
    }

    public void loadUserProfile() {
        _profileState.setValue(new ProfileState.Loading());
        String targetUserId = (profileUserIdArg != null && !profileUserIdArg.trim().isEmpty()) ? profileUserIdArg : currentAuthUserUid;

        Log.d(TAG, "Loading profile for targetUserId: " + targetUserId);

        if (targetUserId == null) {
            _profileState.postValue(new ProfileState.Error("Không xác định được người dùng để hiển thị hồ sơ."));
            return;
        }

        final String finalTargetUserId = targetUserId;

        userRepository.getUserProfile(finalTargetUserId, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    itemRepository.getItemsBySellerId(finalTargetUserId, new Callback<List<Item>>() {
                        @Override
                        public void onSuccess(List<Item> items) {
                            boolean isCurrentUser = finalTargetUserId.equals(currentAuthUserUid);
                            _profileState.postValue(new ProfileState.Success(user, items != null ? items : Collections.emptyList(), isCurrentUser));
                        }

                        @Override
                        public void onFailure(@NonNull Exception itemException) {
                            boolean isCurrentUser = finalTargetUserId.equals(currentAuthUserUid);
                            _profileState.postValue(new ProfileState.Success(user, Collections.emptyList(), isCurrentUser));
                            Log.e(TAG, "Failed to load user items for " + finalTargetUserId + ": " + itemException.getMessage());
                        }
                    });
                } else {
                    _profileState.postValue(new ProfileState.Error("Không tìm thấy thông tin người dùng cho ID: " + finalTargetUserId));
                }
            }

            @Override
            public void onFailure(@NonNull Exception userException) {
                _profileState.postValue(new ProfileState.Error("Lỗi tải thông tin người dùng cho ID " + finalTargetUserId + ": " + userException.getMessage()));
            }
        });
    }

    public void resetUpdateProfileState() {
        _updateProfileState.setValue(new UpdateProfileState.Idle());
    }

    // Hàm này nhận 4 tham số, không có Context vì ViewModel đã có applicationContext
    public void updateUserProfile(
            @NonNull String updatedDisplayName,
            @Nullable String updatedBio,
            @NonNull ContactInfo updatedContactInfo,
            @Nullable Uri newProfileImageUri
    ) {
        _updateProfileState.setValue(new UpdateProfileState.Loading());
        User userToUpdateFromLiveData = _currentUserForEdit.getValue();

        if (currentAuthUserUid == null || userToUpdateFromLiveData == null) {
            _updateProfileState.postValue(new UpdateProfileState.Error("Không thể cập nhật, người dùng không xác định."));
            return;
        }

        // Tạo một bản sao user để cập nhật, tránh thay đổi trực tiếp user từ LiveData
        // Điều này quan trọng nếu userToUpdateFromLiveData được dùng ở nơi khác
        final User userToUpdate = new User();
        userToUpdate.setUid(userToUpdateFromLiveData.getUid());
        userToUpdate.setEmail(userToUpdateFromLiveData.getEmail());
        userToUpdate.setCreatedAt(userToUpdateFromLiveData.getCreatedAt());
        userToUpdate.setAverageRating(userToUpdateFromLiveData.getAverageRating());
        userToUpdate.setTotalTransactions(userToUpdateFromLiveData.getTotalTransactions());
        userToUpdate.setTotalListings(userToUpdateFromLiveData.getTotalListings());
        userToUpdate.setFcmTokens(userToUpdateFromLiveData.getFcmTokens());
        userToUpdate.setLocation(userToUpdateFromLiveData.getLocation());
        userToUpdate.setDeactivated(userToUpdateFromLiveData.isDeactivated());
        userToUpdate.setLastLoginAt(userToUpdateFromLiveData.getLastLoginAt());
        // Quan trọng: giữ lại ảnh cũ nếu không có ảnh mới
        userToUpdate.setProfilePictureUrl(userToUpdateFromLiveData.getProfilePictureUrl());


        // Gán các giá trị đã cập nhật
        userToUpdate.setDisplayName(updatedDisplayName);
        userToUpdate.setBio(updatedBio);
        userToUpdate.setContactInfo(updatedContactInfo);


        if (newProfileImageUri != null) {
            CloudinaryUploader.uploadImageDirectlyToCloudinary(this.applicationContext, newProfileImageUri, new CloudinaryUploader.CloudinaryUploadCallback() {
                @Override
                public void onSuccess(@NonNull String imageUrl) {
                    Log.d(TAG, "New image uploaded to Cloudinary: " + imageUrl);
                    userToUpdate.setProfilePictureUrl(imageUrl); // Cập nhật URL ảnh mới
                    performFirestoreProfileUpdate(userToUpdate);
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    _updateProfileState.postValue(new UpdateProfileState.Error("Lỗi tải ảnh lên: " + e.getMessage()));
                    Log.e(TAG, "Cloudinary upload failed.", e);
                }

                @Override
                public void onErrorResponse(int code, @Nullable String errorMessage) {
                    _updateProfileState.postValue(new UpdateProfileState.Error("Lỗi từ Cloudinary (" + code + "): " + errorMessage));
                    Log.e(TAG, "Cloudinary error response ("+code+"): " + errorMessage);
                }
            });
        } else {
            // Không có ảnh mới, cập nhật thông tin text với ảnh cũ (đã được set ở trên)
            performFirestoreProfileUpdate(userToUpdate);
        }
    }

    private void performFirestoreProfileUpdate(@NonNull User userToUpdate) {
        // updatedAt sẽ được xử lý bởi @ServerTimestamp trong model User.java
        // hoặc bạn có thể set thủ công FieldValue.serverTimestamp() nếu dùng Map để update.
        // Hiện tại, model User đã có @ServerTimestamp cho updatedAt nên không cần set ở đây.

        userRepository.updateUserProfile(userToUpdate, new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                _updateProfileState.postValue(new UpdateProfileState.Success("Hồ sơ đã được cập nhật thành công!"));
                fetchCurrentUserForEditing(); // Tải lại dữ liệu mới cho _currentUserForEdit
                // Nếu ProfileFragment cũng đang hiển thị profile này, nó sẽ tự cập nhật khi _profileState thay đổi
                if (Objects.equals(userToUpdate.getUid(), profileUserIdArg) || (profileUserIdArg == null && Objects.equals(userToUpdate.getUid(), currentAuthUserUid))) {
                    loadUserProfile(); // Trigger load lại profile đang xem nếu đó là profile vừa được sửa
                }
                Log.d(TAG, "Profile updated successfully in Firestore.");
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                _updateProfileState.postValue(new UpdateProfileState.Error("Lỗi cập nhật hồ sơ: " + exception.getMessage()));
                Log.e(TAG, "Firestore profile update failed: " + exception.getMessage());
            }
        });
    }

    public void refreshProfile() {
        loadUserProfile();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ProfileViewModel onCleared");
    }
}