package com.example.tradeup.ui.edit;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.CloudinaryUploader;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.ItemRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditItemViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final String itemId;

    private final MutableLiveData<Item> _item = new MutableLiveData<>();
    public LiveData<Item> getItem() { return _item; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _errorMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getErrorMessage() { return _errorMessage; }

    private final MutableLiveData<Event<Boolean>> _updateSuccessEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getUpdateSuccessEvent() { return _updateSuccessEvent; }

    @Inject
    public EditItemViewModel(ItemRepository itemRepository, SavedStateHandle savedStateHandle) {
        this.itemRepository = itemRepository;
        this.itemId = savedStateHandle.get("itemId");
        loadItemDetails();
    }

    private void loadItemDetails() {
        if (itemId == null) {
            _errorMessage.setValue(new Event<>("Item ID is missing."));
            return;
        }
        _isLoading.setValue(true);
        itemRepository.getItemById(itemId, new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                _isLoading.postValue(false);
                if (item != null) _item.postValue(item);
                else _errorMessage.postValue(new Event<>("Item not found."));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue(new Event<>("Failed to load item: " + e.getMessage()));
            }
        });
    }

    public void saveChanges(String title, String desc, String priceStr, List<String> existingUrls, List<Uri> newUris) {
        Item currentItem = _item.getValue();
        if (currentItem == null) {
            _errorMessage.setValue(new Event<>("Original item data not loaded."));
            return;
        }

        // Validation...
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            _errorMessage.setValue(new Event<>("Invalid price."));
            return;
        }

        _isLoading.setValue(true);

        if (!newUris.isEmpty()) {
            uploadNewImages(title, desc, price, existingUrls, newUris);
        } else {
            updateItemInFirestore(title, desc, price, existingUrls);
        }
    }

    private void uploadNewImages(String title, String desc, double price, List<String> existingUrls, List<Uri> newUris) {
        final List<String> allImageUrls = Collections.synchronizedList(new ArrayList<>(existingUrls));
        final AtomicInteger counter = new AtomicInteger(0);

        for (Uri uri : newUris) {
            // Giả sử CloudinaryUploader có hàm upload với Context. Bạn cần truyền nó vào từ Fragment.
            // CloudinaryUploader.uploadImage...
            // Trong onSuccess:
            // allImageUrls.add(imageUrl);
            // if (counter.incrementAndGet() == newUris.size()) {
            //     updateItemInFirestore(title, desc, price, allImageUrls);
            // }
        }
        // Tạm thời bỏ qua logic upload phức tạp, giả sử thành công
        _errorMessage.setValue(new Event<>("Image uploading is not fully implemented yet."));
        _isLoading.setValue(false);
    }

    private void updateItemInFirestore(String title, String desc, double price, List<String> finalImageUrls) {
        Item itemToUpdate = _item.getValue();
        if (itemToUpdate == null) return;

        itemToUpdate.setTitle(title);
        itemToUpdate.setDescription(desc);
        itemToUpdate.setPrice(price);
        itemToUpdate.setImageUrls(finalImageUrls);

        itemRepository.updateItem(itemToUpdate, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.postValue(false);
                _updateSuccessEvent.postValue(new Event<>(true));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorMessage.postValue(new Event<>("Failed to save: " + e.getMessage()));
            }
        });
    }
}