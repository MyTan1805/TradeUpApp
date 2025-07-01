// File: src/main/java/com/example/tradeup/ui/listing/AddItemViewModel.java

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        loadAppConfig();
    }

    public void loadAppConfig() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) { _appConfig.postValue(config); }
            @Override
            public void onFailure(Exception e) { _addItemState.postValue(new AddItemState.Error("Lỗi tải cấu hình ứng dụng.")); }
        });
    }

    public void postItem(Item itemToPost, List<Uri> imageUris) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _addItemState.postValue(new AddItemState.Error("Bạn cần đăng nhập để đăng tin."));
            return;
        }

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
                itemToPost.setStatus("available");

                // *** THÊM LOGIC TẠO KEYWORDS ***
                itemToPost.setSearchKeywords(generateKeywords(itemToPost.getTitle()));

                uploadImagesAndPostItem(itemToPost, imageUris);
            }
            @Override
            public void onFailure(Exception e) { _addItemState.postValue(new AddItemState.Error("Lỗi lấy thông tin người dùng: " + e.getMessage())); }
        });
    }

    private void uploadImagesAndPostItem(Item item, List<Uri> imageUris) {
        if (imageUris == null || imageUris.isEmpty()) {
            _addItemState.postValue(new AddItemState.Error("Vui lòng chọn ít nhất một ảnh."));
            return;
        }

        _addItemState.postValue(new AddItemState.Loading("Đang tải ảnh lên... (0/" + imageUris.size() + ")"));

        final List<String> uploadedImageUrls = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger completedTasks = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);
        final int totalImages = imageUris.size();

        for (Uri imageUri : imageUris) {
            CloudinaryUploader.uploadImageDirectlyToCloudinary(applicationContext, imageUri, new CloudinaryUploader.CloudinaryUploadCallback() {
                private void handleUploadResult() {
                    int finished = completedTasks.incrementAndGet();
                    if (finished < totalImages) {
                        _addItemState.postValue(new AddItemState.Loading("Đang tải ảnh " + finished + "/" + totalImages));
                    }
                    if (finished == totalImages) {
                        if (failureCount.get() > 0) {
                            _addItemState.postValue(new AddItemState.Error("Tải lên " + failureCount.get() + " ảnh thất bại. Vui lòng thử lại."));
                        } else {
                            item.setImageUrls(uploadedImageUrls);
                            saveItemToFirestore(item);
                        }
                    }
                }
                @Override
                public void onSuccess(@NonNull String imageUrl) {
                    uploadedImageUrls.add(imageUrl);
                    handleUploadResult();
                }
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Image upload failed", e);
                    failureCount.incrementAndGet();
                    handleUploadResult();
                }
                @Override
                public void onErrorResponse(int code, @Nullable String errorMessage) {
                    Log.e(TAG, "Cloudinary error " + code + ": " + errorMessage);
                    failureCount.incrementAndGet();
                    handleUploadResult();
                }
            });
        }
    }

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

    // *** HÀM HELPER MỚI ***
    private List<String> generateKeywords(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String cleanTitle = title.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String[] words = cleanTitle.split("\\s+");
        Set<String> keywords = new HashSet<>(Arrays.asList(words));
        keywords.add(cleanTitle);
        return new ArrayList<>(keywords);
    }
}