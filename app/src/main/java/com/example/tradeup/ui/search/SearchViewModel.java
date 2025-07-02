package com.example.tradeup.ui.search;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final AppConfigRepository appConfigRepository;
    private final MutableLiveData<SearchScreenState> _screenState = new MutableLiveData<>(new SearchScreenState.Idle());
    public LiveData<SearchScreenState> getScreenState() { return _screenState; }

    // LiveData cho các bộ lọc
    private final MutableLiveData<String> _keyword = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedCategoryId = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedConditionId = new MutableLiveData<>();
    private final MutableLiveData<Double> _minPrice = new MutableLiveData<>();
    private final MutableLiveData<Double> _maxPrice = new MutableLiveData<>();
    private final MutableLiveData<SortOrder> _sortOrder = new MutableLiveData<>(SortOrder.NEWEST);

    // LiveData cho trạng thái UI
    private final MutableLiveData<List<Item>> _searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> _error = new MutableLiveData<>();
    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();

    private final MutableLiveData<Event<String>> _errorToast = new MutableLiveData<>();
    public LiveData<Event<String>> getErrorToast() { return _errorToast; }

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final MutableLiveData<Double> _searchRadiusInMeters = new MutableLiveData<>();

    public enum SortOrder {
        NEWEST("Newest", "createdAt", Query.Direction.DESCENDING),
        PRICE_ASC("Price: Low to High", "price", Query.Direction.ASCENDING),
        PRICE_DESC("Price: High to Low", "price", Query.Direction.DESCENDING);

        public final String displayName;
        public final String field;
        public final Query.Direction direction;

        SortOrder(String displayName, String field, Query.Direction direction) {
            this.displayName = displayName;
            this.field = field;
            this.direction = direction;
        }
    }

    @Inject
    public SearchViewModel(ItemRepository itemRepository, AppConfigRepository appConfigRepository) {
        this.itemRepository = itemRepository;
        this.appConfigRepository = appConfigRepository;
        loadAppConfig();
    }


    private void loadAppConfig() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig result) {
                _appConfig.postValue(result);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _error.postValue(new Event<>("Failed to load app config: " + e.getMessage()));
            }
        });
    }

    private void executeSearch() {
        _screenState.postValue(new SearchScreenState.Loading());

        // << FIX: Gọi hàm searchItems với đúng các tham số đã được tinh gọn >>
        itemRepository.searchItems(
                _keyword.getValue(),
                _selectedCategoryId.getValue(),
                _selectedConditionId.getValue(),
                _minPrice.getValue(),
                _maxPrice.getValue(),
                20, // Giới hạn số lượng kết quả trả về
                new Callback<List<Item>>() {
                    @Override
                    public void onSuccess(List<Item> items) {
                        if (items == null || items.isEmpty()) {
                            _screenState.postValue(new SearchScreenState.Empty());
                        } else {
                            _screenState.postValue(new SearchScreenState.Success(items));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        _screenState.postValue(new SearchScreenState.Error("Search failed: " + e.getMessage()));
                    }
                });
    }

    private void triggerSearch(boolean withDelay) {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        searchRunnable = this::executeSearch;

        if (withDelay) {
            searchHandler.postDelayed(searchRunnable, 200); // 200ms debounce
        } else {
            searchHandler.post(searchRunnable);
        }
    }

    // --- PUBLIC METHODS FOR UI ---

    public void setKeyword(@Nullable String keyword) {
        if (!Objects.equals(_keyword.getValue(), keyword)) {
            _keyword.setValue(keyword);
            triggerSearch(true); // Debounce
        }
    }

    public void submitKeyword(@Nullable String keyword) {
        _keyword.setValue(keyword);
        triggerSearch(false); // No debounce
    }

    public void setCategoryAndSearch(@Nullable String categoryId) {
        if (!Objects.equals(_selectedCategoryId.getValue(), categoryId)) {
            _selectedCategoryId.setValue(categoryId);
            triggerSearch(false);
        }
    }

    public void setPriceRangeAndSearch(@Nullable Double min, @Nullable Double max) {
        if (!Objects.equals(_minPrice.getValue(), min) || !Objects.equals(_maxPrice.getValue(), max)) {
            _minPrice.setValue(min);
            _maxPrice.setValue(max);
            triggerSearch(false);
        }
    }

    public void setConditionAndSearch(@Nullable String conditionId) {
        if (!Objects.equals(_selectedConditionId.getValue(), conditionId)) {
            _selectedConditionId.setValue(conditionId);
            triggerSearch(false);
        }
    }

    public void setSortOrderAndSearch(SortOrder sortOrder) {
        if (_sortOrder.getValue() != sortOrder) {
            _sortOrder.setValue(sortOrder);
            triggerSearch(false);
        }
    }

    // --- GETTERS FOR LIVEDATA ---

    public LiveData<String> getSelectedCategoryId() { return _selectedCategoryId; }
    public LiveData<String> getSelectedConditionId() { return _selectedConditionId; }
    public LiveData<Double> getMinPrice() { return _minPrice; }
    public LiveData<Double> getMaxPrice() { return _maxPrice; }
    public LiveData<SortOrder> getSortOrder() { return _sortOrder; }
    public LiveData<List<Item>> getSearchResults() { return _searchResults; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<Event<String>> getError() { return _error; }
    public LiveData<AppConfig> getAppConfig() { return _appConfig; }
}