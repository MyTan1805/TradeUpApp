package com.example.tradeup.ui.details;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.data.network.NotificationApiService;
import com.example.tradeup.data.network.NotificationRequest;
import com.example.tradeup.data.network.NotificationResponse;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.example.tradeup.data.repository.UserSavedItemsRepository;
import com.google.firebase.auth.FirebaseUser;
import retrofit2.Call;
import retrofit2.Response;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.HashMap;
import java.util.Map;

@HiltViewModel
public class ItemDetailViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;
    private final UserSavedItemsRepository userSavedItemsRepository;
    private final AuthRepository authRepository;
    private final NotificationApiService notificationApiService;

    private final MediatorLiveData<ItemDetailViewState> _viewState = new MediatorLiveData<>();
    public LiveData<ItemDetailViewState> getViewState() { return _viewState; }

    private final MutableLiveData<Item> _item = new MutableLiveData<>();
    private final MutableLiveData<User> _seller = new MutableLiveData<>();
    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isBookmarked = new MutableLiveData<>(false);
    public LiveData<Boolean> isBookmarked() { return _isBookmarked; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Boolean> _isViewingOwnItem = new MutableLiveData<>(false);
    public LiveData<Boolean> isViewingOwnItem() { return _isViewingOwnItem; }

    private final String currentUserId;

    @Inject
    public ItemDetailViewModel(
            ItemRepository itemRepository,
            UserRepository userRepository,
            AppConfigRepository appConfigRepository,
            UserSavedItemsRepository userSavedItemsRepository,
            AuthRepository authRepository,
            NotificationApiService notificationApiService,
            SavedStateHandle savedStateHandle
    ) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
        this.userSavedItemsRepository = userSavedItemsRepository;
        this.authRepository = authRepository;
        this.notificationApiService = notificationApiService;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = (user != null) ? user.getUid() : null;

        String itemId = savedStateHandle.get("itemId");

        _viewState.addSource(_item, item -> combineData());
        _viewState.addSource(_seller, seller -> combineData());
        _viewState.addSource(_appConfig, appConfig -> combineData());
        _viewState.addSource(_isBookmarked, isBookmarked -> combineData());

        loadInitialData(itemId);
    }

    private void loadInitialData(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            _viewState.postValue(new ItemDetailViewState.Error("Invalid product ID."));
            return;
        }

        _viewState.setValue(new ItemDetailViewState.Loading());

        itemRepository.incrementItemViews(itemId);

        loadAppConfigFromRepo();
        loadItemFromRepo(itemId);
        checkIfItemIsSaved(itemId);
    }

    private void loadItemFromRepo(String itemId) {
        itemRepository.getItemById(itemId, new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item != null) {
                    _item.postValue(item);
                    loadSellerProfile(item.getSellerId());

                    if (currentUserId != null && currentUserId.equals(item.getSellerId())) {
                        _isViewingOwnItem.postValue(true);
                    } else {
                        _isViewingOwnItem.postValue(false);
                    }
                } else {
                    _viewState.postValue(new ItemDetailViewState.Error("Không tìm thấy sản phẩm."));
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _viewState.postValue(new ItemDetailViewState.Error("Lỗi tải sản phẩm: " + e.getMessage()));
            }
        });
    }

    private void checkIfItemIsSaved(String itemId) {
        if (currentUserId == null) {
            _isBookmarked.postValue(false);
            return;
        }
        userSavedItemsRepository.isItemSaved(currentUserId, itemId, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean isSaved) { _isBookmarked.postValue(isSaved != null ? isSaved : false); }
            @Override
            public void onFailure(@NonNull Exception e) { _isBookmarked.postValue(false); }
        });
    }

    public void toggleBookmark() {
        Item item = _item.getValue();
        Boolean isCurrentlySaved = _isBookmarked.getValue();
        if (currentUserId == null || item == null || isCurrentlySaved == null) {
            _toastMessage.postValue(new Event<>("Bạn cần đăng nhập để thực hiện chức năng này."));
            return;
        }
        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                boolean newState = !isCurrentlySaved;
                _isBookmarked.postValue(newState);
                _toastMessage.postValue(new Event<>(newState ? "Đã lưu sản phẩm!" : "Đã xóa khỏi danh sách lưu."));
            }
            @Override
            public void onFailure(Exception e) { _toastMessage.postValue(new Event<>("Lỗi: " + e.getMessage())); }
        };
        if (isCurrentlySaved) {
            userSavedItemsRepository.unsaveItem(currentUserId, item.getItemId(), callback);
        } else {
            userSavedItemsRepository.saveItem(currentUserId, item.getItemId(), callback);
        }
    }

    private void loadSellerProfile(String sellerId) {
        userRepository.getUserProfile(sellerId, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) { _seller.postValue(user); }
                else { _viewState.postValue(new ItemDetailViewState.Error("Không tìm thấy thông tin người bán.")); }
            }
            @Override
            public void onFailure(@NonNull Exception e) { _viewState.postValue(new ItemDetailViewState.Error("Lỗi tải thông tin người bán: " + e.getMessage())); }
        });
    }

    private void loadAppConfigFromRepo() {
        appConfigRepository.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) { _appConfig.postValue(config); }
            @Override
            public void onFailure(@NonNull Exception e) { _appConfig.postValue(null); }
        });
    }

    private void combineData() {
        Item item = _item.getValue();
        User seller = _seller.getValue();
        AppConfig config = _appConfig.getValue();
        Boolean isBookmarked = _isBookmarked.getValue();
        if (item != null && seller != null && isBookmarked != null) {
            String categoryName = findCategoryName(item.getCategory(), config);
            String conditionName = findConditionName(item.getCondition(), config);
            _viewState.postValue(new ItemDetailViewState.Success(item, seller, categoryName, conditionName, isBookmarked));
        }
    }

    private String findCategoryName(String categoryId, @Nullable AppConfig config) {
        if (config == null || categoryId == null || config.getDisplayCategories() == null) return "N/A";
        return config.getDisplayCategories().stream()
                .filter(cat -> categoryId.equals(cat.getId()))
                .map(DisplayCategoryConfig::getName)
                .findFirst()
                .orElse(categoryId);
    }

    private String findConditionName(String conditionId, @Nullable AppConfig config) {
        if (config == null || conditionId == null || config.getItemConditions() == null) return "N/A";
        return config.getItemConditions().stream()
                .filter(cond -> conditionId.equals(cond.getId()))
                .map(ItemConditionConfig::getName)
                .findFirst()
                .orElse(conditionId);
    }

    public void sendOfferNotification(String sellerId, String itemTitle, String offerId, String buyerDisplayName) {
        if (currentUserId == null) {
            _toastMessage.postValue(new Event<>("Bạn cần đăng nhập để gửi thông báo."));
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "offer");
        data.put("offerId", offerId);
        data.put("itemId", _item.getValue().getItemId());

        NotificationRequest request = new NotificationRequest(
                sellerId,
                "New Offer Received",
                "You have a new offer from " + buyerDisplayName + " for " + itemTitle,
                data
        );

        notificationApiService.sendNotification(request).enqueue(new retrofit2.Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body().success) {
                    _toastMessage.postValue(new Event<>("Notification sent to " + response.body().successCount + " devices"));
                } else {
                    _toastMessage.postValue(new Event<>("Failed to send notification: " + (response.body() != null ? response.body().error : "Unknown error")));
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                _toastMessage.postValue(new Event<>("Error sending notification: " + t.getMessage()));
            }
        });
    }
}