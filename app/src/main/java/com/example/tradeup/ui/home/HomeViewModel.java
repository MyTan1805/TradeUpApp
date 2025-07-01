// File: src/main/java/com/example/tradeup/ui/home/HomeViewModel.java

package com.example.tradeup.ui.home;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
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

    private static final String TAG = "HomeViewModel";
    private static final int PAGE_SIZE = 10;

    private final AppConfigRepository appConfigRepository;
    private final ItemRepository itemRepository;

    private final MutableLiveData<List<DisplayCategoryConfig>> _categories = new MutableLiveData<>();
    public LiveData<List<DisplayCategoryConfig>> getCategories() { return _categories; }

    private final MutableLiveData<List<Item>> _recentItems = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Item>> getRecentItems() { return _recentItems; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    // SỬA LỖI: Thay đổi kiểu dữ liệu của LiveData để chứa Event
    private final MutableLiveData<Event<String>> _errorMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getErrorMessage() { return _errorMessage; }

    // Biến quản lý phân trang
    private String lastVisibleItemId = null;
    private boolean isLastPage = false;
    private boolean isLoadingMore = false;

    @Inject
    public HomeViewModel(AppConfigRepository appConfigRepository, ItemRepository itemRepository) {
        this.appConfigRepository = appConfigRepository;
        this.itemRepository = itemRepository;
        fetchInitialData();
    }

    public void fetchInitialData() {
        fetchCategories();
        fetchRecentItems(true); // true = refresh
    }

    public void refreshData() {
        // Chỉ refresh nếu không có tác vụ nào đang chạy
        if (Boolean.FALSE.equals(_isLoading.getValue()) && !isLoadingMore) {
            fetchRecentItems(true);
        }
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
                // SỬA LỖI: Bọc String trong Event
                _errorMessage.postValue(new Event<>("Lỗi tải danh mục: " + e.getMessage()));
                Log.e(TAG, "Failed to fetch categories", e);
            }
        });
    }

    public void fetchRecentItems(boolean isRefresh) {
        // Kiểm tra để tránh gọi nhiều lần khi đang tải hoặc đã hết dữ liệu
        if (isLoadingMore || (isLastPage && !isRefresh)) {
            return;
        }

        isLoadingMore = true;
        if (isRefresh) {
            _isLoading.setValue(true);
            isLastPage = false;
            lastVisibleItemId = null;
        }

        Log.d(TAG, "Fetching items. Refresh: " + isRefresh + ", LastVisibleId: " + lastVisibleItemId);

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
                Log.e(TAG, "Failed to fetch recent items", e);
                // SỬA LỖI: Bọc String trong Event
                _errorMessage.postValue(new Event<>("Lỗi tải sản phẩm: " + e.getMessage()));
                if (isRefresh) _isLoading.postValue(false);
                isLoadingMore = false;
            }
        });
    }

    public boolean isLastPage() { return isLastPage; }
    public boolean isLoadingMore() { return isLoadingMore; }
}