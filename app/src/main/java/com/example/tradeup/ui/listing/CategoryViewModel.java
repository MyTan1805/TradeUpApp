package com.example.tradeup.ui.listing;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.ItemRepository;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final ItemRepository itemRepository;

    private final MutableLiveData<List<Item>> _items = new MutableLiveData<>();
    public LiveData<List<Item>> getItems() { return _items; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    @Inject
    public CategoryViewModel(ItemRepository itemRepository, SavedStateHandle savedStateHandle) {
        this.itemRepository = itemRepository;

        String categoryId = savedStateHandle.get("categoryId");
        String[] itemIds = savedStateHandle.get("itemIds");

        _isLoading.setValue(true);
        if (itemIds != null && itemIds.length > 0) {
            loadItemsByIds(Arrays.asList(itemIds));
        } else if (categoryId != null) {
            loadItemsByCategory(categoryId);
        } else {
            _error.setValue("No category or item IDs provided.");
            _isLoading.setValue(false);
        }
    }

    public void loadItemsByCategory(String categoryId) {
        itemRepository.getItemsByCategory(categoryId, new Callback<List<Item>>() {
            @Override public void onSuccess(List<Item> data) {
                _items.postValue(data);
                _isLoading.postValue(false);
            }
            @Override public void onFailure(@NonNull Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void loadItemsByIds(List<String> itemIds) {
        itemRepository.getItemsByIds(itemIds, new Callback<List<Item>>() {
            @Override public void onSuccess(List<Item> data) {
                _items.postValue(data);
                _isLoading.postValue(false);
            }
            @Override public void onFailure(@NonNull Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }
}