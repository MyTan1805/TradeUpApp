// File: src/main/java/com/example/tradeup/ui/listing/AddItemViewModel.java

package com.example.tradeup.ui.listing;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.ItemLocation;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.StorageRepository;
import com.google.firebase.auth.FirebaseUser;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddItemViewModel extends ViewModel {

    // --- Repositories ---
    private final ItemRepository itemRepository;
    private final AppConfigRepository appConfigRepository;
    private final StorageRepository storageRepository;
    private final AuthRepository authRepository; // Repository để lấy thông tin người dùng

    // --- LiveData cho UI ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<String>> _postSuccessEvent = new MutableLiveData<>();
    public LiveData<Event<String>> getPostSuccessEvent() { return _postSuccessEvent; }

    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();
    public LiveData<AppConfig> getAppConfig() { return _appConfig; }

    /**
     * Constructor được Hilt sử dụng để inject các dependency.
     */
    @Inject
    public AddItemViewModel(
            ItemRepository itemRepository,
            AppConfigRepository appConfigRepository,
            StorageRepository storageRepository,
            AuthRepository authRepository // Hilt sẽ tự động cung cấp instance
    ) {
        this.itemRepository = itemRepository;
        this.appConfigRepository = appConfigRepository;
        this.storageRepository = storageRepository;
        this.authRepository = authRepository; // Khởi tạo
        loadAppConfig();
    }

    /**
     * Tải cấu hình chung của ứng dụng (danh mục, tình trạng, ...).
     */
    private void loadAppConfig() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override public void onSuccess(AppConfig data) {
                _appConfig.postValue(data);
            }
            @Override public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to load app configuration."));
            }
        });
    }

    /**
     * Bắt đầu quy trình đăng một sản phẩm mới.
     * Đây là nơi chứa logic chính: Lấy thông tin user, gán vào item, và bắt đầu upload ảnh.
     * @param item Đối tượng Item chứa dữ liệu từ UI.
     * @param imageUris Danh sách các Uri của ảnh cần upload.
     */
    public void postItem(Item item, List<Uri> imageUris) {
        _isLoading.setValue(true);

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _toastMessage.setValue(new Event<>("You must be logged in to post an item."));
            _isLoading.setValue(false);
            return;
        }

        // --- SỬA LỖI QUAN TRỌNG: GÁN THÔNG TIN NGƯỜI BÁN VÀO ITEM ---
        item.setSellerId(currentUser.getUid());
        item.setSellerDisplayName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous Seller");
        if (currentUser.getPhotoUrl() != null) {
            item.setSellerProfilePictureUrl(currentUser.getPhotoUrl().toString());
        }
        // Khởi tạo các giá trị mặc định cho item mới
        item.setViewsCount(0);
        item.setOffersCount(0);
        item.setStatus("available"); // Mặc định là 'available'

        // Bắt đầu quy trình upload ảnh
        uploadImagesAndPostItem(item, imageUris);
    }

    /**
     * Tải tất cả các ảnh lên Cloudinary. Sau khi tất cả thành công, sẽ gọi hàm để đăng tin.
     */
    private void uploadImagesAndPostItem(Item item, List<Uri> imageUris) {
        final List<String> uploadedImageUrls = new ArrayList<>();
        final AtomicInteger successCount = new AtomicInteger(0);
        final int totalImages = imageUris.size();

        if (totalImages == 0) {
            _isLoading.setValue(false);
            _toastMessage.setValue(new Event<>("Please select at least one image."));
            return;
        }

        for (Uri uri : imageUris) {
            // userId không cần thiết trong logic upload này, có thể truyền null
            storageRepository.uploadProfilePicture(null, uri, new Callback<String>() {
                @Override
                public void onSuccess(String imageUrl) {
                    uploadedImageUrls.add(imageUrl);
                    // Kiểm tra xem đã upload xong tất cả ảnh chưa
                    if (successCount.incrementAndGet() == totalImages) {
                        item.setImageUrls(uploadedImageUrls);
                        createItemDocument(item);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    // Dừng ngay khi có một ảnh upload thất bại
                    _isLoading.setValue(false);
                    _toastMessage.setValue(new Event<>("Image upload failed: " + e.getMessage()));
                    // Có thể cần thêm logic để không thực hiện tiếp các request khác
                }
            });
        }
    }

    /**
     * Lưu đối tượng Item hoàn chỉnh vào Firestore.
     */
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
                _toastMessage.setValue(new Event<>("Failed to post item: " + e.getMessage()));
            }
        });
    }
}