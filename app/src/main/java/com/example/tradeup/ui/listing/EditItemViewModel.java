// File: src/main/java/com/example/tradeup/ui/listing/EditItemViewModel.java

package com.example.tradeup.ui.listing;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.ItemRepository;

import java.util.Map;
import java.util.HashMap;

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

    private final MutableLiveData<Event<String>> _errorEvent = new MutableLiveData<>();
    public LiveData<Event<String>> getErrorEvent() { return _errorEvent; }

    private final MutableLiveData<Event<Boolean>> _updateSuccessEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getUpdateSuccessEvent() { return _updateSuccessEvent; }

    @Inject
    public EditItemViewModel(ItemRepository itemRepository, SavedStateHandle savedStateHandle) {
        this.itemRepository = itemRepository;
        // Hilt sẽ tự động inject argument từ navigation vào SavedStateHandle
        this.itemId = savedStateHandle.get("itemId");
        loadItemDetails();
    }

    private void loadItemDetails() {
        if (itemId == null || itemId.isEmpty()) {
            _errorEvent.setValue(new Event<>("Invalid Item ID."));
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
                    _errorEvent.postValue(new Event<>("Item not found."));
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorEvent.postValue(new Event<>("Failed to load item: " + e.getMessage()));
            }
        });
    }

    // Tạm thời chỉ cho phép sửa các trường text
    public void saveChanges(String newTitle, String newDescription, String newPriceStr) {
        Item currentItem = _item.getValue();
        if (currentItem == null) {
            _errorEvent.setValue(new Event<>("Cannot save, original item data not available."));
            return;
        }

        double newPrice;
        try {
            newPrice = Double.parseDouble(newPriceStr);
        } catch (NumberFormatException e) {
            _errorEvent.setValue(new Event<>("Invalid price format."));
            return;
        }

        // Tạo một Map để chỉ cập nhật các trường cần thiết
        // Thay vì cập nhật cả object, cách này an toàn hơn, tránh ghi đè các giá trị khác
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("description", newDescription);
        updates.put("price", newPrice);

        _isLoading.setValue(true);

        // Giả sử ItemRepository có hàm updateItemFields(itemId, updates, callback)
        // Nếu không, bạn cần tạo nó hoặc dùng hàm updateItem(item) hiện có
        currentItem.setTitle(newTitle);
        currentItem.setDescription(newDescription);
        currentItem.setPrice(newPrice);

        itemRepository.updateItem(currentItem, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.postValue(false);
                _updateSuccessEvent.postValue(new Event<>(true));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _errorEvent.postValue(new Event<>("Failed to update item: " + e.getMessage()));
            }
        });
    }
}