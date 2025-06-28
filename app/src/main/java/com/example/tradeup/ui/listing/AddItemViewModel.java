// package: com.example.tradeup.ui.listing
package com.example.tradeup.ui.listing;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

// State class giữ nguyên
abstract class AddItemState {
    private AddItemState() {}
    static final class Idle extends AddItemState {}
    static final class Loading extends AddItemState {
        public final String message;
        Loading(String message) { this.message = message; }
    }
    static final class Success extends AddItemState {
        public final String itemId;
        Success(String itemId) { this.itemId = itemId; }
    }
    static final class Error extends AddItemState {
        public final String message;
        Error(String message) { this.message = message; }
    }
}


@HiltViewModel
public class AddItemViewModel extends ViewModel {
    private static final String TAG = "AddItemViewModel";

    private final Context applicationContext;
    private final ItemRepository itemRepository;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<AddItemState> _addItemState = new MutableLiveData<>(new AddItemState.Idle());
    public LiveData<AddItemState> getAddItemState() { return _addItemState; }

    @Inject
    public AddItemViewModel(@ApplicationContext Context context, ItemRepository itemRepository, AuthRepository authRepository, UserRepository userRepository) {
        this.applicationContext = context;
        this.itemRepository = itemRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public void postItem(Item itemToPost, List<Uri> imageUris) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _addItemState.postValue(new AddItemState.Error("Bạn cần đăng nhập để đăng tin."));
            return;
        }

        // Dùng setValue() ở đây là AN TOÀN vì hàm này được gọi từ Fragment (UI Thread)
        _addItemState.setValue(new AddItemState.Loading("Đang lấy thông tin người dùng..."));

        userRepository.getUserProfile(currentUser.getUid(), new Callback<User>() {
            @Override
            public void onSuccess(User userProfile) {
                if (userProfile == null) {
                    _addItemState.postValue(new AddItemState.Error("Không tìm thấy hồ sơ người dùng."));
                    return;
                }

                itemToPost.setSellerId(userProfile.getUid());
                itemToPost.setSellerDisplayName(userProfile.getDisplayName());
                itemToPost.setSellerProfilePictureUrl(userProfile.getProfilePictureUrl());

                uploadImagesAndPostItem(itemToPost, imageUris);
            }

            @Override
            public void onFailure(Exception e) {
                // Dùng postValue() vì callback có thể không ở trên UI Thread
                _addItemState.postValue(new AddItemState.Error("Lỗi lấy thông tin người dùng: " + e.getMessage()));
            }
        });
    }

    private void uploadImagesAndPostItem(Item item, List<Uri> imageUris) {
        if (imageUris == null || imageUris.isEmpty()) {
            _addItemState.postValue(new AddItemState.Error("Vui lòng chọn ít nhất một ảnh."));
            return;
        }

        // Dùng postValue() vì hàm này được gọi từ callback của getUserProfile
        _addItemState.postValue(new AddItemState.Loading("Đang tải ảnh lên..."));

        // Sử dụng một List đồng bộ để tránh lỗi khi nhiều thread cùng ghi vào
        final List<String> uploadedImageUrls = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger uploadCounter = new AtomicInteger(0);
        final int totalImages = imageUris.size();

        for (Uri imageUri : imageUris) {
            CloudinaryUploader.uploadImageDirectlyToCloudinary(applicationContext, imageUri, new CloudinaryUploader.CloudinaryUploadCallback() {
                @Override
                public void onSuccess(@NonNull String imageUrl) {
                    uploadedImageUrls.add(imageUrl);
                    int count = uploadCounter.incrementAndGet();

                    // SỬA LỖI: Dùng postValue()
                    _addItemState.postValue(new AddItemState.Loading("Đang tải ảnh " + count + "/" + totalImages));

                    if (count == totalImages) {
                        item.setImageUrls(uploadedImageUrls);
                        saveItemToFirestore(item);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Image upload failed", e);
                    // SỬA LỖI: Dùng postValue()
                    _addItemState.postValue(new AddItemState.Error("Lỗi tải ảnh lên: " + e.getMessage()));
                }

                @Override
                public void onErrorResponse(int code, @Nullable String errorMessage) {
                    // SỬA LỖI: Dùng postValue()
                    _addItemState.postValue(new AddItemState.Error("Lỗi từ Cloudinary (" + code + "): " + errorMessage));
                }
            });
        }
    }

    private void saveItemToFirestore(Item item) {
        // SỬA LỖI: Dùng postValue() vì hàm này được gọi từ callback của uploader
        _addItemState.postValue(new AddItemState.Loading("Đang đăng tin..."));

        itemRepository.addItem(item, new Callback<String>() {
            @Override
            public void onSuccess(String itemId) {
                // SỬA LỖI: Dùng postValue()
                _addItemState.postValue(new AddItemState.Success(itemId));
                Log.d(TAG, "Item posted successfully with ID: " + itemId);
            }

            @Override
            public void onFailure(Exception e) {
                // SỬA LỖI: Dùng postValue()
                _addItemState.postValue(new AddItemState.Error("Lỗi đăng tin: " + e.getMessage()));
                Log.e(TAG, "Failed to post item to Firestore", e);
            }
        });
    }
}