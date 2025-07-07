package com.example.tradeup.ui.search;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
import com.example.tradeup.data.repository.LocationRepository;
import com.example.tradeup.ui.home.HomeState;
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

    private final MutableLiveData<Integer> _distanceInKm = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> _error = new MutableLiveData<>();
    public LiveData<Integer> getDistanceInKm() { return _distanceInKm; }

    private Location searchCenterLocation;
    private final LocationRepository locationRepository;

    private final MutableLiveData<Location> _searchCenter = new MutableLiveData<>();


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

    public void setDistanceAndSearch(@Nullable Integer distanceInKm) {
        _distanceInKm.setValue(distanceInKm);

        if (distanceInKm == null || distanceInKm <= 0) {
            this.searchCenterLocation = null; // Xóa vị trí trung tâm
            triggerSearch(false); // Tìm kiếm lại mà không có vị trí
        } else {
            fetchCurrentUserLocationAndSearch();
        }
    }

    private void fetchCurrentUserLocationAndSearch() {
        _isLoading.setValue(true);
        locationRepository.getCurrentLocation(new Callback<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    searchCenterLocation = location;
                    triggerSearch(false); // Có vị trí rồi, giờ tìm kiếm
                } else {
                    _isLoading.setValue(false);
                    _error.setValue(new Event<>("Could not get current location. Please check GPS and permissions."));
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.setValue(false);
                _error.setValue(new Event<>(e.getMessage()));
            }
        });
    }

    @Inject
    public SearchViewModel(ItemRepository itemRepository, AppConfigRepository appConfigRepository,
                           LocationRepository locationRepository) {
        this.itemRepository = itemRepository;
        this.appConfigRepository = appConfigRepository;
        this.locationRepository = locationRepository;
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

        // Lấy các giá trị bộ lọc
        String keyword = _keyword.getValue();
        String categoryId = _selectedCategoryId.getValue();
        String conditionId = _selectedConditionId.getValue();
        Double minPrice = _minPrice.getValue();
        Double maxPrice = _maxPrice.getValue();
        Integer distance = _distanceInKm.getValue();
        long limit = 20;

        // Lấy giá trị sắp xếp
        SortOrder sortOrder = _sortOrder.getValue();
        if (sortOrder == null) {
            sortOrder = SortOrder.NEWEST; // Mặc định
        }
        final String sortField = sortOrder.field;
        final Query.Direction direction = sortOrder.direction;

        Callback<List<Item>> searchCallback = new Callback<List<Item>>() {
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
                Log.e("SearchViewModel", "Search failed", e);
                _screenState.postValue(new SearchScreenState.Error("Search failed: " + e.getMessage()));
            }
        };

        // Logic quyết định và SỬA LẠI LỜI GỌI HÀM
        if (searchCenterLocation != null && distance != null && distance > 0) {
            // << SỬA LẠI LỜI GỌI HÀM NÀY, THÊM sortField, direction >>
            itemRepository.searchByLocation(
                    searchCenterLocation, distance, keyword, categoryId, conditionId,
                    minPrice, maxPrice, limit,
                    sortField, direction, // <-- Truyền 2 tham số mới
                    searchCallback
            );
        } else {
            // << SỬA LẠI LỜI GỌI HÀM NÀY, THÊM sortField, direction >>
            itemRepository.searchByFilters(
                    keyword, categoryId, conditionId, minPrice, maxPrice, limit,
                    sortField, direction, // <-- Truyền 2 tham số mới
                    searchCallback
            );
        }
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