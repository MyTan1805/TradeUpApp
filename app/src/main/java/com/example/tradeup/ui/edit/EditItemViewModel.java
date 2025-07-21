package com.example.tradeup.ui.edit;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.ItemRepository;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import ch.hsr.geohash.GeoHash;

@HiltViewModel
public class EditItemViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final Context context;
    private final String itemId;
    private final MutableLiveData<Item> _item = new MutableLiveData<>();
    public LiveData<Item> getItem() { return _item; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _errorMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getErrorMessage() { return _errorMessage; }

    private final MutableLiveData<Event<Boolean>> _updateSuccessEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getUpdateSuccessEvent() { return _updateSuccessEvent; }

    private static final int GEOHASH_PRECISION = 9; // Tăng độ chính xác cho Geohash

    @Inject
    public EditItemViewModel(ItemRepository itemRepository, SavedStateHandle savedStateHandle, @ApplicationContext Context context) {
        this.itemRepository = itemRepository;
        this.context = context;
        this.itemId = savedStateHandle.get("itemId");
        loadItemDetails();
    }

    private void loadItemDetails() {
        if (itemId == null) {
            _errorMessage.setValue(new Event<>("Missing product ID."));
            return;
        }
        _isLoading.setValue(true);
        itemRepository.getItemById(itemId, new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                _isLoading.postValue(false);
                if (item != null) {
                    _item.postValue(item);
                } else {
                    _errorMessage.postValue(new Event<>("Product not found."));
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue(new Event<>("Failed to load product: " + e.getMessage()));
            }
        });
    }

    public void updateCondition(String newConditionId) {
        Item currentItem = _item.getValue();
        if (currentItem != null) {
            currentItem.setCondition(newConditionId);
            _item.setValue(currentItem); // Post lại giá trị để giữ trạng thái
        }
    }

    public void saveChanges(
            String title, String desc, String priceStr,
            List<String> existingUrls, List<Uri> newUris,
            GeoPoint geoPoint, String addressString
    ) {
        Item currentItem = _item.getValue();
        if (currentItem == null) {
            _errorMessage.setValue(new Event<>("Original product data not loaded."));
            return;
        }

        // Validation
        if (title == null || title.trim().isEmpty()) {
            _errorMessage.setValue(new Event<>("Title cannot be empty."));
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                _errorMessage.setValue(new Event<>("Price cannot be negative."));
                return;
            }
        } catch (NumberFormatException e) {
            _errorMessage.setValue(new Event<>("Invalid price format."));
            return;
        }
        if (geoPoint == null || addressString == null) {
            _errorMessage.setValue(new Event<>("Location cannot be empty."));
            return;
        }

        _isLoading.setValue(true);

        // Cập nhật thông tin
        currentItem.setTitle(title.trim());
        currentItem.setDescription(desc != null ? desc.trim() : "");
        currentItem.setPrice(price);
        currentItem.setLocation(geoPoint);
        currentItem.setAddressString(addressString);
        currentItem.setGeohash(GeoHash.withCharacterPrecision(
                geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                GEOHASH_PRECISION
        ).toBase32());

        if (!newUris.isEmpty()) {
            uploadNewImages(currentItem, existingUrls, newUris);
        } else {
            currentItem.setImageUrls(existingUrls);
            updateItemInFirestore(currentItem);
        }
    }

    private void uploadNewImages(Item item, List<String> existingUrls, List<Uri> newUris) {
        final List<String> allImageUrls = Collections.synchronizedList(new ArrayList<>(existingUrls));
        final AtomicInteger counter = new AtomicInteger(0);
        final int totalImages = newUris.size();

        for (Uri uri : newUris) {
            CloudinaryUploader.uploadImageDirectlyToCloudinary(context, uri, new CloudinaryUploader.CloudinaryUploadCallback() {
                @Override
                public void onSuccess(@NonNull String imageUrl) {
                    allImageUrls.add(imageUrl);
                    if (counter.incrementAndGet() == totalImages) {
                        item.setImageUrls(allImageUrls);
                        updateItemInFirestore(item);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    _isLoading.setValue(false);
                    _errorMessage.setValue(new Event<>("Image upload failed: " + e.getMessage()));
                }

                @Override
                public void onErrorResponse(int code, @Nullable String errorMessage) {
                    _isLoading.setValue(false);
                    _errorMessage.setValue(new Event<>("Cloudinary Error (" + code + "): " + errorMessage));
                }
            });
        }
    }

    private void updateItemInFirestore(Item item) {
        itemRepository.updateItem(item, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.postValue(false);
                _updateSuccessEvent.postValue(new Event<>(true));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue(new Event<>("Update failed: " + e.getMessage()));
            }
        });
    }
}