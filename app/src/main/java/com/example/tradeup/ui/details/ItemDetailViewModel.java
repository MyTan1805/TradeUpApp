package com.example.tradeup.ui.details;

import static com.example.tradeup.ui.listing.ListingOptionsDialogFragment.TAG;

import android.os.Bundle;
import android.util.Log;
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
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.data.model.config.SubcategoryConfig;
import com.example.tradeup.data.network.NotificationApiService;
import com.example.tradeup.data.network.NotificationRequest;
import com.example.tradeup.data.network.NotificationResponse;
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.example.tradeup.data.repository.UserSavedItemsRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Response;

@HiltViewModel
public class ItemDetailViewModel extends ViewModel {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AppConfigRepository appConfigRepository;
    private final UserSavedItemsRepository userSavedItemsRepository;
    private final AuthRepository authRepository;
    private final NotificationApiService notificationApiService;
    private final FirebaseFirestore firestore;
    private final ChatRepository chatRepository;

    // LiveData cho UI
    private final MediatorLiveData<ItemDetailViewState> _viewState = new MediatorLiveData<>();
    public LiveData<ItemDetailViewState> getViewState() { return _viewState; }

    private final MutableLiveData<Event<Bundle>> _navigateToChatEvent = new MutableLiveData<>();
    public LiveData<Event<Bundle>> getNavigateToChatEvent() { return _navigateToChatEvent; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Boolean> _isViewingOwnItem = new MutableLiveData<>(false);
    public LiveData<Boolean> isViewingOwnItem() { return _isViewingOwnItem; }

    private final MutableLiveData<Boolean> _isCreatingChat = new MutableLiveData<>(false);
    public LiveData<Boolean> isCreatingChat() { return _isCreatingChat; }

    // Dữ liệu thô để Mediator gộp lại
    private final MutableLiveData<Item> _item = new MutableLiveData<>();
    private final MutableLiveData<User> _seller = new MutableLiveData<>();
    private final MutableLiveData<AppConfig> _appConfig = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isBookmarked = new MutableLiveData<>(false);
    public LiveData<Boolean> isBookmarked() { return _isBookmarked; }

    private final String currentUserId;

    @Inject
    public ItemDetailViewModel(
            ItemRepository itemRepository, UserRepository userRepository, AppConfigRepository appConfigRepository,
            UserSavedItemsRepository userSavedItemsRepository, AuthRepository authRepository,
            ChatRepository chatRepository, FirebaseFirestore firestore,
            NotificationApiService notificationApiService, SavedStateHandle savedStateHandle
    ) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.appConfigRepository = appConfigRepository;
        this.userSavedItemsRepository = userSavedItemsRepository;
        this.authRepository = authRepository;
        this.chatRepository = chatRepository;
        this.notificationApiService = notificationApiService;
        this.firestore = firestore;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = (user != null) ? user.getUid() : null;

        // Thêm các nguồn dữ liệu vào MediatorLiveData
        _viewState.addSource(_item, item -> combineData());
        _viewState.addSource(_seller, seller -> combineData());
        _viewState.addSource(_appConfig, appConfig -> combineData());
        _viewState.addSource(_isBookmarked, isBookmarked -> combineData());

        // Lấy dữ liệu từ arguments
        String itemId = savedStateHandle.get("itemId");
        Item itemPreview = savedStateHandle.get("itemPreview");

        // Phân luồng logic chính xác
        if (itemPreview != null) {
            loadPreviewData(itemPreview);
        } else if (itemId != null) {
            loadInitialData(itemId);
        } else {
            _viewState.setValue(new ItemDetailViewState.Error("No item data provided."));
        }
    }

    private void loadPreviewData(Item itemToPreview) {
        _viewState.setValue(new ItemDetailViewState.Loading());
        _isViewingOwnItem.setValue(true);
        _isBookmarked.setValue(false);

        userRepository.getUserProfile(itemToPreview.getSellerId())
                .whenComplete((seller, throwable) -> {
                    if (throwable != null || seller == null) {
                        _viewState.postValue(new ItemDetailViewState.Error("Could not load your profile for preview."));
                        return;
                    }
                    _seller.postValue(seller);
                    appConfigRepository.getAppConfig(new Callback<AppConfig>() {
                        @Override public void onSuccess(AppConfig data) {
                            _appConfig.postValue(data);
                            _item.postValue(itemToPreview);
                        }
                        @Override public void onFailure(@NonNull Exception e) {
                            _viewState.postValue(new ItemDetailViewState.Error("Could not load app config for preview."));
                        }
                    });
                });
    }

    private void loadInitialData(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            _viewState.setValue(new ItemDetailViewState.Error("Invalid product ID."));
            return;
        }
        _viewState.setValue(new ItemDetailViewState.Loading());
        itemRepository.incrementItemViews(itemId);
        loadAppConfigFromRepo();
        loadItemFromRepo(itemId);
        checkIfItemIsSaved(itemId);
    }

    public void onMessageSellerClicked() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        User seller = _seller.getValue();
        Item item = _item.getValue();

        if (currentUser == null) {
            _toastMessage.postValue(new Event<>("Please log in to message the seller."));
            return;
        }
        if (item == null || seller == null) {
            _toastMessage.postValue(new Event<>("Item or seller information is not available."));
            return;
        }
        if (currentUser.getUid().equals(seller.getUid())) {
            _toastMessage.postValue(new Event<>("You cannot message yourself."));
            return;
        }

        _isCreatingChat.setValue(true);

        // Lấy thông tin đầy đủ của người dùng hiện tại
        userRepository.getUserProfile(currentUser.getUid())
                .whenComplete((currentUserInfo, throwable) -> {
                    if (throwable != null || currentUserInfo == null) {
                        _isCreatingChat.postValue(false);
                        _toastMessage.postValue(new Event<>("Could not load your profile to start chat."));
                        return;
                    }

                    // Bây giờ gọi hàm getOrCreateChat với đầy đủ 6 tham số
                    chatRepository.getOrCreateChat(
                            currentUser.getUid(), currentUserInfo,
                            seller.getUid(), seller,
                            item.getItemId(),
                            new Callback<String>() {
                                @Override
                                public void onSuccess(String chatId) {
                                    itemRepository.incrementItemChats(item.getItemId());
                                    _isCreatingChat.postValue(false);

                                    Bundle args = new Bundle();
                                    args.putString("chatId", chatId);
                                    args.putString("otherUserName", seller.getDisplayName());
                                    _navigateToChatEvent.postValue(new Event<>(args));
                                }

                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    _isCreatingChat.postValue(false);
                                    _toastMessage.postValue(new Event<>("Could not start conversation: " + e.getMessage()));
                                }
                            }
                    );
                });
    }

    private void loadItemFromRepo(String itemId) {
        itemRepository.getItemById(itemId, new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item != null) {
                    _item.postValue(item);
                    loadSellerProfile(item.getSellerId());
                    logBrowsingHistory(item);

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

    private void logBrowsingHistory(Item item) {
        if (currentUserId == null || currentUserId.equals(item.getSellerId())) {
            // Không ghi lại lịch sử nếu người dùng xem sản phẩm của chính mình
            return;
        }

        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("userId", currentUserId);
        historyEntry.put("itemId", item.getItemId());
        historyEntry.put("categoryId", item.getCategory());
        historyEntry.put("viewedAt", FieldValue.serverTimestamp());

        firestore.collection("userBrowsingHistory")
                .add(historyEntry)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Browsing history logged for user " + currentUserId))
                .addOnFailureListener(e -> Log.e(TAG, "Error logging browsing history", e));
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
        userRepository.getUserProfile(sellerId)
                .whenComplete((user, throwable) -> {
                    if (throwable != null) {
                        _viewState.postValue(new ItemDetailViewState.Error("Lỗi tải thông tin người bán: " + throwable.getMessage()));
                    } else if (user != null) {
                        _seller.postValue(user);
                    } else {
                        _viewState.postValue(new ItemDetailViewState.Error("Không tìm thấy thông tin người bán."));
                    }
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
        if (config == null || categoryId == null || config.getCategories() == null) return "N/A";

        // Duyệt qua tất cả các danh mục cha
        for (CategoryConfig parentCat : config.getCategories()) {
            // Kiểm tra xem ID có khớp với danh mục cha không
            if (categoryId.equals(parentCat.getId())) {
                return parentCat.getName();
            }
            // Nếu không, duyệt qua các danh mục con của nó
            if (parentCat.getSubcategories() != null) {
                for (SubcategoryConfig subCat : parentCat.getSubcategories()) {
                    if (categoryId.equals(subCat.getId())) {
                        // Trả về tên theo định dạng "Cha > Con"
                        return parentCat.getName() + " > " + subCat.getName();
                    }
                }
            }
        }

        // Nếu không tìm thấy ở đâu, trả về ID gốc
        return categoryId;
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