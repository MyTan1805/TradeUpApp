// File: src/main/java/com/example/tradeup/ui/search/SearchViewModel.java

package com.example.tradeup.ui.search;

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

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final AppConfigRepository appConfigRepository;

    private final MutableLiveData<String> _keyword = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedCategoryId = new MutableLiveData<>();
    private final MutableLiveData<Double> _minPrice = new MutableLiveData<>();
    private final MutableLiveData<Double> _maxPrice = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedConditionId = new MutableLiveData<>();
    private final MutableLiveData<List<Item>> _searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> _error = new MutableLiveData<>();
    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();

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

    public void search() {
        _isLoading.setValue(true);

        String keyword = _keyword.getValue();
        String categoryId = _selectedCategoryId.getValue();
        Double minPrice = _minPrice.getValue();
        Double maxPrice = _maxPrice.getValue();
        String conditionId = _selectedConditionId.getValue();

        itemRepository.searchItems(keyword, categoryId, minPrice, maxPrice, conditionId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                _isLoading.postValue(false);
                _searchResults.postValue(items);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _error.postValue(new Event<>("Search failed: " + e.getMessage()));
            }
        });
    }

    public void setKeywordAndSearch(@Nullable String keyword) {
        _keyword.setValue(keyword);
        search();
    }

    public void setCategoryAndSearch(@Nullable String categoryId) {
        _selectedCategoryId.setValue(categoryId);
        search();
    }

    public void setPriceRangeAndSearch(@Nullable Double min, @Nullable Double max) {
        _minPrice.setValue(min);
        _maxPrice.setValue(max);
        search();
    }

    public void setConditionAndSearch(@Nullable String conditionId) {
        _selectedConditionId.setValue(conditionId);
        search();
    }

    public LiveData<String> getKeyword() {
        return _keyword;
    }

    public LiveData<String> getSelectedCategoryId() {
        return _selectedCategoryId;
    }

    public LiveData<Double> getMinPrice() {
        return _minPrice;
    }

    public LiveData<Double> getMaxPrice() {
        return _maxPrice;
    }

    public LiveData<String> getSelectedConditionId() {
        return _selectedConditionId;
    }

    public LiveData<List<Item>> getSearchResults() {
        return _searchResults;
    }

    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    public LiveData<Event<String>> getError() {
        return _error;
    }

    public LiveData<AppConfig> getAppConfig() {
        return _appConfig;
    }
}