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
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
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

@HiltViewModel
public class AddItemViewModel extends ViewModel {
    private static final String TAG = "AddItemViewModel";

    private final Context applicationContext;
    private final ItemRepository itemRepository;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;

    private final MutableLiveData<AddItemState> _addItemState = new MutableLiveData<>(new AddItemState.Idle());
    public LiveData<AddItemState> getAddItemState() { return _addItemState; }

    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();
    public LiveData<AppConfig> getAppConfig() { return _appConfig; }

    @Inject
    public AddItemViewModel(@ApplicationContext Context context,
                            ItemRepository itemRepository,
                            AuthRepository authRepository,
                            UserRepository userRepository,
                            AppConfigRepository appConfigRepository) {
        this.applicationContext = context;
        this.itemRepository = itemRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
        loadAppConfig(); // Tải cấu hình ngay khi ViewModel được tạo
    }

    /**
     * Tải cấu hình chung của ứng dụng (danh mục, tình trạng, etc.)
     */
    public void loadAppConfig() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                _appConfig.postValue(config);
            }
            @Override
            public void onFailure(Exception e) {
                _addItemState.postValue(new AddItemState.Error("Lỗi tải cấu hình ứng dụng."));
            }
        });
    }

    /**
     * Bắt đầu quy trình đăng tin.
     * Quy trình: Lấy thông tin người dùng -> Tải ảnh -> Lưu tin lên Firestore.
     */
    public void postItem(Item itemToPost, List<Uri> imageUris) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _addItemState.postValue(new AddItemState.Error("Bạn cần đăng nhập để đăng tin."));
            return;
        }

        _addItemState.setValue(new AddItemState.Loading("Đang lấy thông tin người dùng..."));

        // Bước 1: Lấy thông tin hồ sơ người dùng để gán vào sản phẩm
        userRepository.getUserProfile(currentUser.getUid(), new Callback<User>() {
            @Override
            public void onSuccess(User userProfile) {
                if (userProfile == null) {
                    _addItemState.postValue(new AddItemState.Error("Không tìm thấy hồ sơ người dùng."));
                    return;
                }

                // Gán thông tin người bán vào item
                itemToPost.setSellerId(userProfile.getUid());
                itemToPost.setSellerDisplayName(userProfile.getDisplayName());
                itemToPost.setSellerProfilePictureUrl(userProfile.getProfilePictureUrl());
                itemToPost.setStatus("available"); // Mặc định là có sẵn

                // Bước 2: Bắt đầu tải ảnh
                uploadImagesAndPostItem(itemToPost, imageUris);
            }

            @Override
            public void onFailure(Exception e) {
                _addItemState.postValue(new AddItemState.Error("Lỗi lấy thông tin người dùng: " + e.getMessage()));
            }
        });
    }

    /**
     * Tải các ảnh đã chọn lên dịch vụ lưu trữ (Cloudinary).
     * Sau khi tất cả ảnh được tải lên, tiếp tục lưu thông tin item.
     */
    private void uploadImagesAndPostItem(Item item, List<Uri> imageUris) {
        if (imageUris == null || imageUris.isEmpty()) {
            _addItemState.postValue(new AddItemState.Error("Vui lòng chọn ít nhất một ảnh."));
            return;
        }

        _addItemState.postValue(new AddItemState.Loading("Đang tải ảnh lên... (0/" + imageUris.size() + ")"));

        final List<String> uploadedImageUrls = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger uploadCounter = new AtomicInteger(0);
        final int totalImages = imageUris.size();

        for (Uri imageUri : imageUris) {
            CloudinaryUploader.uploadImageDirectlyToCloudinary(applicationContext, imageUri, new CloudinaryUploader.CloudinaryUploadCallback() {
                @Override
                public void onSuccess(@NonNull String imageUrl) {
                    uploadedImageUrls.add(imageUrl);
                    int count = uploadCounter.incrementAndGet();
                    _addItemState.postValue(new AddItemState.Loading("Đang tải ảnh " + count + "/" + totalImages));

                    // Nếu đã tải xong tất cả ảnh
                    if (count == totalImages) {
                        item.setImageUrls(uploadedImageUrls);
                        // Bước 3: Đăng tin lên Firestore
                        saveItemToFirestore(item);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Image upload failed", e);
                    _addItemState.postValue(new AddItemState.Error("Lỗi tải ảnh lên: " + e.getMessage()));
                }

                @Override
                public void onErrorResponse(int code, @Nullable String errorMessage) {
                    _addItemState.postValue(new AddItemState.Error("Lỗi từ Cloudinary (" + code + "): " + errorMessage));
                }
            });
        }
    }

    /**
     * Lưu đối tượng Item hoàn chỉnh lên Firestore.
     */
    private void saveItemToFirestore(Item item) {
        _addItemState.postValue(new AddItemState.Loading("Đang đăng tin..."));

        itemRepository.addItem(item, new Callback<String>() {
            @Override
            public void onSuccess(String itemId) {
                _addItemState.postValue(new AddItemState.Success(itemId));
                Log.d(TAG, "Item posted successfully with ID: " + itemId);
            }

            @Override
            public void onFailure(Exception e) {
                _addItemState.postValue(new AddItemState.Error("Lỗi đăng tin: " + e.getMessage()));
                Log.e(TAG, "Failed to post item to Firestore", e);
            }
        });
    }
}