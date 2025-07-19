// File: src/main/java/com/example/tradeup/ui/profile/EditProfileViewModel.java
package com.example.tradeup.ui.profile;

import android.app.Application;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.ContactInfo;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.StorageRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture; // Thêm import này
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final StorageRepository storageRepository;

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
    public EditProfileViewModel(Application application, UserRepository userRepository, AuthRepository authRepository, StorageRepository storageRepository) {
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
            // Sử dụng CompletableFuture ở đây luôn cho nhất quán
            userRepository.getUserProfile(currentUserId)
                    .whenComplete((user, throwable) -> {
                        if (throwable != null) {
                            _errorEvent.postValue(new Event<>("Failed to load user data: " + throwable.getMessage()));
                        } else {
                            _user.postValue(user);
                        }
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

        // Bước 1: Chuẩn bị CompletableFuture cho URL ảnh
        CompletableFuture<String> imageUrlFuture;
        if (newProfilePictureUri != null) {
            // Nếu có ảnh mới, tạo Future để tải ảnh lên
            imageUrlFuture = uploadProfilePictureAsync(currentUserId, newProfilePictureUri);
        } else {
            // Nếu không, dùng URL cũ và bọc trong một Future đã hoàn thành
            imageUrlFuture = CompletableFuture.completedFuture(_user.getValue().getProfilePictureUrl());
        }

        // Bước 2: Xâu chuỗi việc cập nhật Firestore sau khi có URL ảnh
        imageUrlFuture.thenCompose(imageUrl -> {
            // Bước 3: Cập nhật tài liệu người dùng
            return updateUserDocument(newDisplayName, newBio, newPhoneNumber, imageUrl);
        }).whenComplete((aVoid, throwable) -> {
            _isLoading.postValue(false);
            if (throwable != null) {
                _errorEvent.postValue(new Event<>("Failed to save profile: " + throwable.getMessage()));
            } else {
                _saveSuccessEvent.postValue(new Event<>(true));
                loadCurrentUser(); // Tải lại dữ liệu mới
            }
        });
    }

    private CompletableFuture<String> uploadProfilePictureAsync(String userId, Uri imageUri) {
        CompletableFuture<String> future = new CompletableFuture<>();
        storageRepository.uploadProfilePicture(userId, imageUri, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                future.complete(data);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private CompletableFuture<Void> updateUserDocument(String displayName, String bio, String phoneNumber, String profilePictureUrl) {
        User currentUserData = _user.getValue();
        if (currentUserData == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return CompletableFuture.failedFuture(new IllegalStateException("User data not available."));
            }
        }

        ContactInfo contactInfo = currentUserData.getContactInfo() != null ? currentUserData.getContactInfo() : new ContactInfo();
        contactInfo.setPhone(phoneNumber);

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("bio", bio);
        updates.put("profilePictureUrl", profilePictureUrl);
        updates.put("contactInfo", contactInfo);

        return userRepository.updateUserProfile(currentUserId, updates);
    }
}