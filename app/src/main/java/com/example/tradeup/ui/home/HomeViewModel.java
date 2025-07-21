package com.example.tradeup.ui.home;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    // Repositories
    private final Context appContext;
    private final AppConfigRepository appConfigRepository;
    private final ItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final UserSavedItemsRepository userSavedItemsRepository;
    private final FirebaseFirestore firestore;
    private final Executor backgroundExecutor;

    // Dữ liệu thô từ các nguồn
    private final MutableLiveData<List<Item>> _rawRecentItems = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryConfig>> _rawCategories = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> _savedItemIds = new MutableLiveData<>();

    // State cuối cùng gửi về UI
    private final MediatorLiveData<HomeState> _state = new MediatorLiveData<>();
    public LiveData<HomeState> getState() { return _state; }

    // LiveData cho các phần phụ
    private final MutableLiveData<List<Item>> _recommendedItems = new MutableLiveData<>();
    public LiveData<List<Item>> getRecommendedItems() { return _recommendedItems; }
    private final MutableLiveData<List<Item>> _nearbyItems = new MutableLiveData<>();
    public LiveData<List<Item>> getNearbyItems() { return _nearbyItems; }
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoadingMore() { return _isLoadingMore; }
    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    // Biến trạng thái
    private User currentUserData;
    private String lastVisibleItemId = null;
    private boolean isLastPage = false;
    private boolean isRequestInProgress = false;
    private final String currentUserId;

    @Inject
    public HomeViewModel(AppConfigRepository appConfigRepository, ItemRepository itemRepository,
                         LocationRepository locationRepository, AuthRepository authRepository,
                         UserRepository userRepository, UserSavedItemsRepository userSavedItemsRepository,
                         FirebaseFirestore firestore, @ApplicationContext Context context) {
        this.appConfigRepository = appConfigRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.authRepository = authRepository;
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.userRepository = userRepository;
        this.userSavedItemsRepository = userSavedItemsRepository;
        this.firestore = firestore;
        this.appContext = context;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = (user != null) ? user.getUid() : null;

        _state.addSource(_rawRecentItems, items -> combineDataForState());
        _state.addSource(_rawCategories, categories -> combineDataForState());
        _state.addSource(_savedItemIds, savedIds -> combineDataForState());

        if (this.currentUserId != null) {
            loadCurrentUserInfo(this.currentUserId);
        }
    }

    private void combineDataForState() {
        List<Item> items = _rawRecentItems.getValue();
        List<CategoryConfig> categories = _rawCategories.getValue();
        Set<String> savedIds = _savedItemIds.getValue();

        if (items == null || categories == null || savedIds == null) return;

        if (items.isEmpty() && categories.isEmpty()) {
            _state.setValue(new HomeState.Empty(Collections.emptyList()));
        } else {
            // SỬ DỤNG CONSTRUCTOR MỚI VỚI 3 THAM SỐ
            _state.setValue(new HomeState.Success(categories, items, savedIds));
        }
    }

    public void refreshData() {
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            _state.postValue(new HomeState.Error("No internet connection. Please try again."));
            return;
        }
        if (isRequestInProgress) return;

        isRequestInProgress = true;
        _state.setValue(new HomeState.Loading());
        lastVisibleItemId = null;
        isLastPage = false;

        _rawRecentItems.setValue(null);
        _rawCategories.setValue(null);
        _savedItemIds.setValue(null);

        loadSavedItemIds();
        loadAppConfig();
        if (currentUserId != null) {
            loadRecommendations();
        } else {
            _recommendedItems.postValue(Collections.emptyList());
        }
    }

    private void loadAppConfig() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                _rawCategories.postValue(config != null ? config.getCategories() : Collections.emptyList());
                fetchRecentItems(true);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _state.postValue(new HomeState.Error("Failed to load app configuration."));
                isRequestInProgress = false;
            }
        });
    }

    private void fetchRecentItems(final boolean isRefresh) {
        itemRepository.getAllItems(PAGE_SIZE, lastVisibleItemId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> newItems) {
                backgroundExecutor.execute(() -> {
                    List<Item> filteredItems = newItems;
                    if (currentUserData != null && currentUserData.getBlockedUsers() != null && !currentUserData.getBlockedUsers().isEmpty()) {
                        filteredItems = newItems.stream()
                                .filter(item -> !currentUserData.getBlockedUsers().contains(item.getSellerId()))
                                .collect(Collectors.toList());
                    }

                    if (isRefresh) {
                        _rawRecentItems.postValue(filteredItems);
                        loadNearbyItemsBasedOnLocation();
                    } else {
                        _isLoadingMore.postValue(false);
                        List<Item> currentItems = new ArrayList<>(_rawRecentItems.getValue() != null ? _rawRecentItems.getValue() : Collections.emptyList());
                        currentItems.addAll(filteredItems);
                        _rawRecentItems.postValue(currentItems);
                    }

                    isLastPage = newItems.size() < PAGE_SIZE;
                    if (!newItems.isEmpty()) {
                        lastVisibleItemId = newItems.get(newItems.size() - 1).getItemId();
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

    private void loadCurrentUserInfo(String userId) {
        userRepository.getUserProfile(userId).whenComplete((user, throwable) -> {
            if (throwable != null) {
                Log.e(TAG, "Failed to load current user profile", throwable);
                currentUserData = null;
            } else {
                currentUserData = user;
            }
        });
    }

    private void loadSavedItemIds() {
        if (currentUserId == null) {
            _savedItemIds.postValue(new HashSet<>());
            return;
        }
        userSavedItemsRepository.getSavedItemIds(currentUserId, new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                _savedItemIds.postValue(new HashSet<>(data));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _savedItemIds.postValue(new HashSet<>());
                Log.e(TAG, "Failed to load saved item IDs", e);
            }
        });
    }

    public void toggleFavoriteStatus(Item item) {
        if (currentUserId == null) {
            _toastMessage.setValue(new Event<>("Please log in to save items."));
            return;
        }
        Set<String> currentSavedIds = new HashSet<>(_savedItemIds.getValue() != null ? _savedItemIds.getValue() : new HashSet<>());
        boolean isCurrentlySaved = currentSavedIds.contains(item.getItemId());

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isCurrentlySaved) {
                    currentSavedIds.remove(item.getItemId());
                } else {
                    currentSavedIds.add(item.getItemId());
                }
                _savedItemIds.postValue(currentSavedIds);
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

    public void fetchMoreItems() {
        if (isRequestInProgress || isLastPage) return;
        isRequestInProgress = true;
        _isLoadingMore.setValue(true);
        fetchRecentItems(false);
    }

    private void loadNearbyItemsBasedOnLocation() {
        locationRepository.getCurrentLocation(new Callback<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    if (isRequestInProgress) isRequestInProgress = false;
                    _nearbyItems.postValue(Collections.emptyList());
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

    private void loadRecommendations() {
        if (currentUserId == null) return;
        firestore.collection("userBrowsingHistory")
                .whereEqualTo("userId", currentUserId)
                .orderBy("viewedAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        _recommendedItems.postValue(Collections.emptyList());
                        return;
                    }
                    Map<String, Integer> categoryCounts = new HashMap<>();
                    List<String> viewedItemIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String categoryId = doc.getString("categoryId");
                        String itemId = doc.getString("itemId");
                        if (categoryId != null) {
                            categoryCounts.put(categoryId, categoryCounts.getOrDefault(categoryId, 0) + 1);
                        }
                        if (itemId != null) {
                            viewedItemIds.add(itemId);
                        }
                    }
                    if (categoryCounts.isEmpty()) {
                        _recommendedItems.postValue(Collections.emptyList());
                        return;
                    }
                    String favoriteCategoryId = Collections.max(categoryCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
                    if (favoriteCategoryId != null) {
                        fetchItemsFromFavoriteCategory(favoriteCategoryId, viewedItemIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting browsing history", e);
                    _recommendedItems.postValue(Collections.emptyList());
                });
    }

    private void fetchItemsFromFavoriteCategory(String categoryId, List<String> excludedItemIds) {
        itemRepository.getItemsByCategory(categoryId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> data) {
                if (data != null && !data.isEmpty()) {
                    List<Item> recommendations = data.stream()
                            .filter(item -> !currentUserId.equals(item.getSellerId()) && !excludedItemIds.contains(item.getItemId()))
                            .limit(10)
                            .collect(Collectors.toList());
                    _recommendedItems.postValue(recommendations);
                } else {
                    _recommendedItems.postValue(Collections.emptyList());
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error fetching recommended items", e);
                _recommendedItems.postValue(Collections.emptyList());
            }
        });
    }
}