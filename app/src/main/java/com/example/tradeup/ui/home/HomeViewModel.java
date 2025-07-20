// File: src/main/java/com/example/tradeup/ui/home/HomeViewModel.java
package com.example.tradeup.ui.home;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.core.utils.NetworkUtils;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.LocationRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.example.tradeup.data.repository.UserSavedItemsRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";
    private static final int PAGE_SIZE = 10;

    private final Context appContext;
    private final AppConfigRepository appConfigRepository;
    private final ItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final AuthRepository authRepository;
    private final Executor backgroundExecutor;
    private final UserRepository userRepository;

    private final UserSavedItemsRepository userSavedItemsRepository;

    private User currentUserData;

    private final MutableLiveData<Set<String>> _savedItemIds = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> getSavedItemIds() { return _savedItemIds; }

    private final MutableLiveData<HomeState> _state = new MutableLiveData<>();
    public LiveData<HomeState> getState() { return _state; }

    private final MutableLiveData<List<Item>> _nearbyItems = new MutableLiveData<>();
    public LiveData<List<Item>> getNearbyItems() { return _nearbyItems; }

    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoadingMore() { return _isLoadingMore; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private String lastVisibleItemId = null;
    private boolean isLastPage = false;
    private boolean isRequestInProgress = false;
    private final String currentUserId;

    @Inject
    public HomeViewModel(AppConfigRepository appConfigRepository, ItemRepository itemRepository,
                         LocationRepository locationRepository, AuthRepository authRepository,
                         UserRepository userRepository,UserSavedItemsRepository userSavedItemsRepository,
                         @ApplicationContext Context context) {
        this.appConfigRepository = appConfigRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.authRepository = authRepository;
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.userRepository = userRepository;
        this.userSavedItemsRepository = userSavedItemsRepository;
        this.appContext = context;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = (user != null) ? user.getUid() : null;

        if (this.currentUserId != null) {
            loadCurrentUserInfo(this.currentUserId);
        }
    }

    private void loadCurrentUserInfo(String userId) {
        userRepository.getUserProfile(userId)
                .whenComplete((user, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Failed to load current user profile", throwable);
                        currentUserData = null;
                    } else {
                        currentUserData = user;
                    }
                });
    }

    private void loadSavedItemIds() {
        if (currentUserId == null) return;
        userSavedItemsRepository.getSavedItemIds(currentUserId, new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                _savedItemIds.postValue(new HashSet<>(data));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load saved item IDs", e);
            }
        });
    }

    // << THÊM HÀM MỚI NÀY ĐỂ XỬ LÝ SỰ KIỆN CLICK >>
    public void toggleFavoriteStatus(Item item) {
        if (currentUserId == null) {
            _toastMessage.setValue(new Event<>("Please log in to save items."));
            return;
        }

        Set<String> currentSavedIds = _savedItemIds.getValue();
        if (currentSavedIds == null) {
            currentSavedIds = new HashSet<>();
        }

        boolean isCurrentlySaved = currentSavedIds.contains(item.getItemId());

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Cập nhật lại danh sách ID đã lưu để UI tự động thay đổi
                loadSavedItemIds();
                _toastMessage.postValue(new Event<>(isCurrentlySaved ? "Unsaved item" : "Item saved!"));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Action failed: " + e.getMessage()));
            }
        };

        if (isCurrentlySaved) {
            userSavedItemsRepository.unsaveItem(currentUserId, item.getItemId(), callback);
        } else {
            userSavedItemsRepository.saveItem(currentUserId, item.getItemId(), callback);
        }
    }


    public void refreshData() {
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            _state.postValue(new HomeState.Error("No internet connection. Please try again."));
            return;
        }
        if (isRequestInProgress) {
            return;
        }
        isRequestInProgress = true;
        _state.postValue(new HomeState.Loading());
        lastVisibleItemId = null;
        isLastPage = false;

        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                backgroundExecutor.execute(() -> {
                    List<CategoryConfig> categories = (config != null) ? config.getCategories() : Collections.emptyList();
                    fetchRecentItems(true, categories);
                });
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _state.postValue(new HomeState.Error("Failed to load app configuration."));
                isRequestInProgress = false;
            }
        });
    }

    private void fetchRecentItems(final boolean isRefresh, @Nullable final List<CategoryConfig> categories) {
        itemRepository.getAllItems(PAGE_SIZE, lastVisibleItemId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> newItems) {
                List<Item> filteredItems = newItems;
                if (currentUserData != null && currentUserData.getBlockedUsers() != null && !currentUserData.getBlockedUsers().isEmpty()) {
                    filteredItems = newItems.stream()
                            .filter(item -> !currentUserData.getBlockedUsers().contains(item.getSellerId()))
                            .collect(Collectors.toList());
                }

                final List<Item> finalItems = filteredItems;

                backgroundExecutor.execute(() -> {
                    if (isRefresh) {
                        if (finalItems.isEmpty()) {
                            _state.postValue(new HomeState.Empty(categories));
                        } else {
                            _state.postValue(new HomeState.Success(categories, finalItems));
                        }
                        loadNearbyItemsBasedOnLocation();
                    } else {
                        _isLoadingMore.postValue(false);
                        if (_state.getValue() instanceof HomeState.Success) {
                            HomeState.Success currentState = (HomeState.Success) _state.getValue();
                            List<Item> currentItems = new ArrayList<>(currentState.recentItems);
                            currentItems.addAll(finalItems);
                            _state.postValue(new HomeState.Success(currentState.categories, currentItems));
                        }
                    }

                    isLastPage = (finalItems.size() < PAGE_SIZE);
                    if (!finalItems.isEmpty()) {
                        lastVisibleItemId = finalItems.get(finalItems.size() - 1).getItemId();
                    }
                    if (!isRefresh) {
                        isRequestInProgress = false;
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                if (isRefresh) {
                    _state.postValue(new HomeState.Error("Failed to load items. Please try again."));
                } else {
                    _isLoadingMore.postValue(false);
                    _toastMessage.postValue(new Event<>("Failed to load more items."));
                }
                isRequestInProgress = false;
            }
        });
    }

    public void fetchMoreItems() {
        if (isRequestInProgress || isLastPage) return;
        isRequestInProgress = true;
        _isLoadingMore.setValue(true);
        fetchRecentItems(false, null);
    }

    private void loadNearbyItemsBasedOnLocation() {
        locationRepository.getCurrentLocation(new Callback<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    if (isRequestInProgress) isRequestInProgress = false;
                    return;
                }
                itemRepository.searchByLocation(location, 10, null, null, null, null, null, 20, "createdAt", Query.Direction.DESCENDING, new Callback<List<Item>>() {
                    @Override
                    public void onSuccess(List<Item> items) {
                        backgroundExecutor.execute(() -> {
                            if (items != null && !items.isEmpty()) {
                                List<Item> otherUsersItems = items.stream()
                                        .filter(item -> currentUserId == null || !currentUserId.equals(item.getSellerId()))
                                        .limit(10)
                                        .collect(Collectors.toList());
                                _nearbyItems.postValue(otherUsersItems);
                            } else {
                                _nearbyItems.postValue(Collections.emptyList());
                            }
                            if (isRequestInProgress) isRequestInProgress = false;
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        _nearbyItems.postValue(Collections.emptyList());
                        if (isRequestInProgress) isRequestInProgress = false;
                    }
                });
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _nearbyItems.postValue(Collections.emptyList());
                if (isRequestInProgress) isRequestInProgress = false;
            }
        });
    }
}