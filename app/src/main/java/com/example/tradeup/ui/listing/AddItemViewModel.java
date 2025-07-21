package com.example.tradeup.ui.listing;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.ItemLocation;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import ch.hsr.geohash.GeoHash;

@HiltViewModel
public class AddItemViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final AppConfigRepository appConfigRepository;
    private final AuthRepository authRepository;
    private final Context context;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<String>> _postSuccessEvent = new MutableLiveData<>();
    public LiveData<Event<String>> getPostSuccessEvent() { return _postSuccessEvent; }

    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();
    public LiveData<AppConfig> getAppConfig() { return _appConfig; }

    public final MutableLiveData<List<Uri>> imageUris = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<String> parentCategoryId = new MutableLiveData<>();
    public final MutableLiveData<String> subCategoryId = new MutableLiveData<>();
    public final MutableLiveData<String> conditionId = new MutableLiveData<>();
    public final MutableLiveData<String> itemBehavior = new MutableLiveData<>();
    public final MutableLiveData<ItemLocation> location = new MutableLiveData<>();
    public final MutableLiveData<List<String>> tags = new MutableLiveData<>(new ArrayList<>());


    private static final int GEOHASH_PRECISION = 5;

    public static final List<String> ITEM_BEHAVIORS = Arrays.asList(
            "Pickup only",
            "Shipping available",
            "Pickup or Shipping"
    );

    @Inject
    public AddItemViewModel(
            ItemRepository itemRepository,
            AppConfigRepository appConfigRepository,
            AuthRepository authRepository,
            @ApplicationContext Context context
    ) {
        this.itemRepository = itemRepository;
        this.appConfigRepository = appConfigRepository;
        this.authRepository = authRepository;
        this.context = context;
        loadAppConfig();
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    private void loadAppConfig() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig data) {
                _appConfig.postValue(data);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Không thể tải cấu hình ứng dụng."));
            }
        });
    }

    public void postItem(Item item, List<Uri> imageUris) {
        _isLoading.setValue(true);


        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("Bạn phải đăng nhập để đăng sản phẩm."));
            return;
        }

        // Gán thông tin người bán
        item.setSellerId(currentUser.getUid());
        item.setSellerDisplayName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Người bán ẩn danh");
        if (currentUser.getPhotoUrl() != null) {
            item.setSellerProfilePictureUrl(currentUser.getPhotoUrl().toString());
        }
        item.setViewsCount(0L);
        item.setOffersCount(0L);
        item.setStatus("available");

        // Tính Geohash nếu có GeoPoint
        if (item.getLocation() != null) {
            item.setGeohash(GeoHash.withCharacterPrecision(
                    item.getLocation().getLatitude(),
                    item.getLocation().getLongitude(),
                    GEOHASH_PRECISION
            ).toBase32());
        } else {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("Vị trí không được để trống."));
            return;
        }

        // Kiểm tra imageUris
        if (imageUris.isEmpty()) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("Vui lòng chọn ít nhất một ảnh."));
            return;
        }

        // Tải ảnh lên Cloudinary
        uploadImagesAndPostItem(item, imageUris);
    }

    private void uploadImagesAndPostItem(Item item, List<Uri> imageUris) {
        List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        int totalImages = imageUris.size();

        for (Uri uri : imageUris) {
            CloudinaryUploader.uploadImageDirectlyToCloudinary(context, uri, new CloudinaryUploader.CloudinaryUploadCallback() {
                @Override
                public void onSuccess(@NonNull String imageUrl) {
                    uploadedImageUrls.add(imageUrl);
                    if (successCount.incrementAndGet() == totalImages) {
                        item.setImageUrls(uploadedImageUrls);
                        createItemDocument(item);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    _isLoading.setValue(false);
                    _toastMessage.setValue(new Event<>("Tải ảnh thất bại: " + e.getMessage()));
                }

                @Override
                public void onErrorResponse(int code, @Nullable String errorMessage) {
                    _isLoading.setValue(false);
                    _toastMessage.setValue(new Event<>("Lỗi Cloudinary (" + code + "): " + errorMessage));
                }
            });
        }
    }

    private void createItemDocument(Item item) {
        itemRepository.addItem(item, new Callback<String>() {
            @Override
            public void onSuccess(String itemId) {
                _isLoading.setValue(false);
                _postSuccessEvent.setValue(new Event<>(itemId));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _toastMessage.setValue(new Event<>("Đăng sản phẩm thất bại: " + e.getMessage()));
            }
        });
    }
}