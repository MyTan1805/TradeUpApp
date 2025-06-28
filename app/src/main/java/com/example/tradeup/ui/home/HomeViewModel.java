package com.example.tradeup.ui.home;

import android.util.Log; // Thêm Log
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.ItemRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel"; // Thêm TAG cho logging

    private final AppConfigRepository appConfigRepository;
    private final ItemRepository itemRepository;

    private final MutableLiveData<List<DisplayCategoryConfig>> _categories = new MutableLiveData<>();
    public LiveData<List<DisplayCategoryConfig>> getCategories() { return _categories; }

    // Sẽ chia ra Featured và Recent sau, ban đầu chỉ cần 1 list
    private final MutableLiveData<List<Item>> _recentItems = new MutableLiveData<>();
    public LiveData<List<Item>> getRecentItems() { return _recentItems; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    // Biến để quản lý phân trang
    private String lastVisibleItemId = null;
    private boolean isLoadingMore = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 10;

    // Getter public cho các biến trạng thái
    public boolean isLastPage() {
        return isLastPage;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    @Inject
    public HomeViewModel(AppConfigRepository appConfigRepository, ItemRepository itemRepository) {
        this.appConfigRepository = appConfigRepository;
        this.itemRepository = itemRepository;
        fetchInitialData();
    }

    public void fetchInitialData() {
        if (_isLoading.getValue() != null && _isLoading.getValue()) return;
        Log.d(TAG, "Fetching initial data...");

        _isLoading.setValue(true);
        isLastPage = false; // Reset khi refresh
        lastVisibleItemId = null; // Reset khi refresh
        fetchCategories();
        fetchRecentItems(true); // true để xóa list cũ
    }

    private void fetchCategories() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                if (config != null && config.getDisplayCategories() != null) {
                    _categories.postValue(config.getDisplayCategories());
                } else {
                    _categories.postValue(Collections.emptyList());
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _errorMessage.postValue("Lỗi tải danh mục: " + e.getMessage());
                Log.e(TAG, "Failed to fetch categories", e);
            }
        });
    }

    public void fetchRecentItems(boolean isRefresh) {
        if (isLoadingMore || isLastPage) {
            Log.d(TAG, "fetchRecentItems: Skipped. isLoadingMore=" + isLoadingMore + ", isLastPage=" + isLastPage);
            return;
        }
        isLoadingMore = true;

        if (isRefresh) {
            _isLoading.setValue(true); // Chỉ hiện loading toàn màn hình khi refresh
            lastVisibleItemId = null;
        }

        Log.d(TAG, "Fetching recent items. isRefresh=" + isRefresh + ", lastVisibleId=" + lastVisibleItemId);

        itemRepository.getAllItems(PAGE_SIZE, lastVisibleItemId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> newItems) {
                if (newItems != null) {
                    Log.d(TAG, "Fetched " + newItems.size() + " new items.");
                    if (newItems.size() < PAGE_SIZE) {
                        isLastPage = true;
                        Log.d(TAG, "Last page reached.");
                    }

                    if (!newItems.isEmpty()) {
                        lastVisibleItemId = newItems.get(newItems.size() - 1).getItemId();
                    }

                    List<Item> currentItems = isRefresh ? new ArrayList<>() : (_recentItems.getValue() != null ? new ArrayList<>(_recentItems.getValue()) : new ArrayList<>());
                    currentItems.addAll(newItems);
                    _recentItems.postValue(currentItems);
                }

                if (isRefresh) _isLoading.postValue(false);
                isLoadingMore = false;
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _errorMessage.postValue("Lỗi tải sản phẩm: " + e.getMessage());
                Log.e(TAG, "Failed to fetch recent items", e);
                if (isRefresh) _isLoading.postValue(false);
                isLoadingMore = false;
            }
        });
    }
}