// File: src/main/java/com/example/tradeup/ui/profile/EditProfileViewModel.java
package com.example.tradeup.ui.profile;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.StorageRepository; // Import StorageRepository (phiên bản Cloudinary)
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final StorageRepository storageRepository; // Chèn StorageRepository

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> getUser() { return _user; }
    private final MutableLiveData<Event<String>> _errorEvent = new MutableLiveData<>();
    public LiveData<Event<String>> getErrorEvent() { return _errorEvent; }
    private final MutableLiveData<Event<Boolean>> _saveSuccessEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getSaveSuccessEvent() { return _saveSuccessEvent; }
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private String currentUserId;
    private Uri newProfilePictureUri = null;

    @Inject
    public EditProfileViewModel(
            Application application,
            UserRepository userRepository,
            AuthRepository authRepository,
            StorageRepository storageRepository // Hilt sẽ chèn phiên bản Cloudinary vào đây
    ) {
        super(application);
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.storageRepository = storageRepository;
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        FirebaseUser firebaseUser = authRepository.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            userRepository.getUserProfile(currentUserId, new Callback<User>() {
                @Override
                public void onSuccess(User user) { _user.postValue(user); }
                @Override
                public void onFailure(@NonNull Exception e) { _errorEvent.postValue(new Event<>("Failed to load user data: " + e.getMessage())); }
            });
        } else {
            _errorEvent.postValue(new Event<>("No authenticated user found."));
        }
    }

    public void setNewProfilePicture(Uri imageUri) { this.newProfilePictureUri = imageUri; }

    public void saveChanges(String newDisplayName, String newBio, String newPhoneNumber) {
        if (currentUserId == null || _user.getValue() == null) {
            _errorEvent.postValue(new Event<>("Cannot save, user data not loaded."));
            return;
        }
        _isLoading.postValue(true);

        if (newProfilePictureUri != null) {
            // Bước 1: Tải ảnh lên Cloudinary thông qua StorageRepository
            storageRepository.uploadProfilePicture(currentUserId, newProfilePictureUri, new Callback<String>() {
                @Override
                public void onSuccess(String imageUrl) {
                    // Bước 2: Sau khi có URL từ Cloudinary, cập nhật Firestore
                    updateUserDocument(newDisplayName, newBio, newPhoneNumber, imageUrl);
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    _isLoading.postValue(false);
                    _errorEvent.postValue(new Event<>("Failed to upload new profile picture: " + e.getMessage()));
                }
            });
        } else {
            // Trường hợp không đổi ảnh: Cập nhật Firestore với URL cũ
            updateUserDocument(newDisplayName, newBio, newPhoneNumber, _user.getValue().getProfilePictureUrl());
        }
    }

    private void updateUserDocument(String displayName, String bio, String phoneNumber, String profilePictureUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("bio", bio);
        updates.put("profilePictureUrl", profilePictureUrl); // URL này là từ Cloudinary
        updates.put("contactInfo.phone", phoneNumber);

        userRepository.updateUserProfile(currentUserId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isLoading.postValue(false);
                _saveSuccessEvent.postValue(new Event<>(true));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorEvent.postValue(new Event<>("Failed to save profile: " + e.getMessage()));
            }
        });
    }
}