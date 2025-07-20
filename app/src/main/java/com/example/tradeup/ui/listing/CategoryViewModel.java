// File: src/main/java/com/example/tradeup/ui/listing/CategoryViewModel.java
package com.example.tradeup.ui.listing;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.ItemRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final AppConfigRepository appConfigRepository;

    // LiveData này chứa danh sách sản phẩm sẽ được hiển thị trên UI (đã được lọc/sắp xếp)
    private final MutableLiveData<List<Item>> _displayedItems = new MutableLiveData<>();
    public LiveData<List<Item>> getDisplayedItems() { return _displayedItems; }

    // LiveData này chứa thông tin về danh mục cha đang được xem
    private final MutableLiveData<CategoryConfig> _parentCategory = new MutableLiveData<>();
    public LiveData<CategoryConfig> getParentCategory() { return _parentCategory; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    // Biến private để lưu trữ toàn bộ danh sách item gốc, chưa qua lọc
    private List<Item> allItemsForParentCategory = new ArrayList<>();

    @Inject
    public CategoryViewModel(ItemRepository itemRepository, AppConfigRepository appConfigRepository, SavedStateHandle savedStateHandle) {
        this.itemRepository = itemRepository;
        this.appConfigRepository = appConfigRepository;

        String categoryId = savedStateHandle.get("categoryId");
        String[] itemIds = savedStateHandle.get("itemIds");

        if (categoryId != null) {
            loadParentCategoryInfo(categoryId); // Tải thông tin danh mục cha
            loadItemsByCategory(categoryId);    // Tải sản phẩm
        } else if (itemIds != null && itemIds.length > 0) {
            loadItemsByIds(Arrays.asList(itemIds));
        } else {
            _error.setValue("No category or item IDs provided.");
        }
    }

    // Tải thông tin chi tiết của danh mục cha (để lấy tên và danh sách con)
    private void loadParentCategoryInfo(String categoryId) {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig data) {
                if (data != null && data.getCategories() != null) {
                    data.getCategories().stream()
                            .filter(c -> c.getId().equals(categoryId))
                            .findFirst()
                            .ifPresent(_parentCategory::postValue);
                }
            }
            @Override public void onFailure(@NonNull Exception e) {
                _error.postValue("Failed to load category details.");
            }
        });
    }

    private void loadItemsByCategory(String categoryId) {
        _isLoading.setValue(true);
        itemRepository.getItemsByCategory(categoryId, new Callback<List<Item>>() {
            @Override public void onSuccess(List<Item> data) {
                allItemsForParentCategory = (data != null) ? data : Collections.emptyList();
                _displayedItems.postValue(allItemsForParentCategory); // Hiển thị tất cả ban đầu
                _isLoading.postValue(false);
            }
            @Override public void onFailure(@NonNull Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    private void loadItemsByIds(List<String> itemIds) {
        _isLoading.setValue(true);
        itemRepository.getItemsByIds(itemIds, new Callback<List<Item>>() {
            @Override public void onSuccess(List<Item> data) {
                allItemsForParentCategory = (data != null) ? data : Collections.emptyList();
                _displayedItems.postValue(allItemsForParentCategory);
                _isLoading.postValue(false);
            }
            @Override public void onFailure(@NonNull Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    // Hàm để lọc danh sách sản phẩm theo danh mục con
    public void filterBySubCategory(String subCategoryId) {
        if (subCategoryId == null) { // Tag của chip "All" là null
            // Nếu chọn "All", hiển thị lại toàn bộ danh sách gốc
            _displayedItems.setValue(allItemsForParentCategory);
            return;
        }

        List<Item> filteredList = allItemsForParentCategory.stream()
                .filter(item -> subCategoryId.equals(item.getSubCategory()))
                .collect(Collectors.toList());
        _displayedItems.setValue(filteredList);
    }

    // Hàm để sắp xếp danh sách sản phẩm đang hiển thị
    public void sortItems(Comparator<Item> comparator) {
        List<Item> currentList = _displayedItems.getValue();
        if (currentList == null || currentList.isEmpty()) return;

        ArrayList<Item> sortedList = new ArrayList<>(currentList);
        sortedList.sort(comparator);
        _displayedItems.setValue(sortedList);
    }
}