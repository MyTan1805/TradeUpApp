package com.example.tradeup.ui.details;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.AppConfig;
// === FIX: Import đúng các lớp model con ===
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.ItemRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ItemDetailViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final AppConfigRepository appConfigRepository;

    private final MediatorLiveData<ItemDetailViewState> _viewState = new MediatorLiveData<>();
    public LiveData<ItemDetailViewState> getViewState() { return _viewState; }

    private final MutableLiveData<Item> _item = new MutableLiveData<>();
    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();

    @Inject
    public ItemDetailViewModel(
            ItemRepository itemRepository,
            AppConfigRepository appConfigRepository,
            SavedStateHandle savedStateHandle
    ) {
        this.itemRepository = itemRepository;
        this.appConfigRepository = appConfigRepository;

        String itemId = savedStateHandle.get("itemId");
        loadItemDetails(itemId);

        _viewState.addSource(_item, item -> combineData());
        _viewState.addSource(_appConfig, appConfig -> combineData());
    }

    private void loadItemDetails(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            _viewState.postValue(new ItemDetailViewState.Error("Invalid Item ID."));
            return;
        }
        _viewState.setValue(new ItemDetailViewState.Loading());
        loadItemFromRepo(itemId);
        loadAppConfigFromRepo();
    }

    private void loadItemFromRepo(String itemId) {
        itemRepository.getItemById(itemId, new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item != null) {
                    _item.postValue(item);
                } else {
                    _viewState.postValue(new ItemDetailViewState.Error("Item not found."));
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _viewState.postValue(new ItemDetailViewState.Error("Failed to load item: " + e.getMessage()));
            }
        });
    }

    private void loadAppConfigFromRepo() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                if (config != null) {
                    _appConfig.postValue(config);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                // Có thể bỏ qua lỗi này
            }
        });
    }

    private void combineData() {
        Item item = _item.getValue();
        AppConfig config = _appConfig.getValue();

        if (item != null && config != null) {
            // === FIX: Gọi đúng tên phương thức và dùng đúng kiểu dữ liệu ===
            String categoryName = findCategoryName(item.getCategory(), config.getDisplayCategories());
            String conditionName = findConditionName(item.getCondition(), config.getItemConditions());

            _viewState.postValue(new ItemDetailViewState.Success(item, categoryName, conditionName));
        }
    }

    // === FIX: Sửa lại kiểu dữ liệu của danh sách ===
    private String findCategoryName(String categoryId, List<DisplayCategoryConfig> categories) {
        if (categoryId == null || categories == null) return categoryId;
        for (DisplayCategoryConfig cat : categories) {
            if (categoryId.equals(cat.getId())) {
                return cat.getName();
            }
        }
        return categoryId;
    }

    // === FIX: Sửa lại kiểu dữ liệu của danh sách ===
    private String findConditionName(String conditionId, List<ItemConditionConfig> conditions) {
        if (conditionId == null || conditions == null) return conditionId;
        for (ItemConditionConfig cond : conditions) {
            if (conditionId.equals(cond.getId())) {
                return cond.getName();
            }
        }
        return conditionId;
    }
}