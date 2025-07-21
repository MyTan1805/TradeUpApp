// File: src/main/java/com/example/tradeup/ui/profile/ProfileViewModel.java

package com.example.tradeup.ui.profile;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Rating;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.RatingRepository;
import com.example.tradeup.data.repository.TransactionRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RatingRepository ratingRepository;
    private final TransactionRepository transactionRepository;
    private final AuthRepository authRepository;

    private User loadedUser = null;
    private List<Item> loadedItems = null;

    // LiveData cho trạng thái chung

    private final MutableLiveData<Event<Rating>> _reportReviewEvent = new MutableLiveData<>();
    public LiveData<Event<Rating>> getReportReviewEvent() { return _reportReviewEvent; }
    private final MutableLiveData<ProfileHeaderState> _headerState = new MutableLiveData<>();
    public LiveData<ProfileHeaderState> getHeaderState() { return _headerState; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    // LiveData cho các tab
    private final MutableLiveData<List<Item>> _activeListings = new MutableLiveData<>();
    public LiveData<List<Item>> getActiveListings() { return _activeListings; }

    private final MutableLiveData<List<Transaction>> _soldTransactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getSoldTransactions() { return _soldTransactions; }

    // LiveData này sẽ chứa các Item đã bán, được chuyển đổi từ Transaction
    private final MutableLiveData<List<Item>> _soldItems = new MutableLiveData<>();
    public LiveData<List<Item>> getSoldItems() { return _soldItems; }

    private final MutableLiveData<List<Item>> _pausedListings = new MutableLiveData<>();
    public LiveData<List<Item>> getPausedListings() { return _pausedListings; }

    private final MutableLiveData<List<Rating>> _reviews = new MutableLiveData<>();
    public LiveData<List<Rating>> getReviews() { return _reviews; }

    // LiveData cho các sự kiện và tương tác
    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Item> _selectedItem = new MutableLiveData<>();
    public LiveData<Item> getSelectedItem() { return _selectedItem; }
    public void setSelectedItem(Item item) { _selectedItem.setValue(item); }

    private final String currentAuthUserUid;
    private final String profileUserIdArg;

    @Inject
    public ProfileViewModel(
            AuthRepository authRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            RatingRepository ratingRepository,
            TransactionRepository transactionRepository,
            SavedStateHandle savedStateHandle
    ) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.ratingRepository = ratingRepository;
        this.transactionRepository = transactionRepository;

        FirebaseUser fbUser = authRepository.getCurrentUser();
        this.currentAuthUserUid = (fbUser != null) ? fbUser.getUid() : null;
        this.profileUserIdArg = savedStateHandle.get("userId");

        loadAllData();
    }

    public void loadAllData() {
        _isLoading.setValue(true);
        loadedUser = null;
        loadedItems = null;

        String targetUserId = (profileUserIdArg != null && !profileUserIdArg.trim().isEmpty()) ? profileUserIdArg : currentAuthUserUid;

        if (targetUserId == null) {
            _toastMessage.postValue(new Event<>("User not found."));
            _isLoading.postValue(false);
            return;
        }

        loadUserProfile(targetUserId);
        loadUserListings(targetUserId); // Gộp logic tải item
        loadUserReviews(targetUserId);
    }

    private void loadUserProfile(String userId) {
        // *** SỬA Ở ĐÂY: Chuyển sang dùng CompletableFuture ***
        userRepository.getUserProfile(userId)
                .whenComplete((user, throwable) -> {
                    if (throwable != null) {
                        _headerState.postValue(new ProfileHeaderState.Error("Error loading profile: " + throwable.getMessage()));
                    } else if (user != null) {
                        loadedUser = user;
                        combineAndPostState();
                    } else {
                        _headerState.postValue(new ProfileHeaderState.Error("User profile not found."));
                    }
                });
    }

    private void loadUserListings(String userId) {
        itemRepository.getItemsBySellerId(userId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                loadedItems = (items != null) ? items : new ArrayList<>();
                combineAndPostState(); // Gọi hàm đồng bộ
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Error loading listings: " + e.getMessage()));
                loadedItems = new ArrayList<>(); // Coi như tải xong với danh sách rỗng
                combineAndPostState();
            }
        });
    }

    private synchronized void combineAndPostState() {
        if (loadedUser == null || loadedItems == null) {
            return;
        }

        long activeCount = loadedItems.stream().filter(i -> "available".equalsIgnoreCase(i.getStatus())).count();
        long soldCount = loadedItems.stream().filter(i -> "sold".equalsIgnoreCase(i.getStatus())).count();

        loadedUser.setTotalListings((int) activeCount);
        loadedUser.setTotalTransactions((int) soldCount);

        boolean isCurrentUser = loadedUser.getUid().equals(currentAuthUserUid);
        _headerState.postValue(new ProfileHeaderState.Success(loadedUser, isCurrentUser));

        filterAndPostListings(loadedItems);

        _isLoading.postValue(false);
    }

    private void filterAndPostListings(List<Item> allItems) {
        if (allItems == null) {
            allItems = Collections.emptyList();
        }
        _activeListings.postValue(allItems.stream()
                .filter(item -> "available".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList()));
        _soldItems.postValue(allItems.stream()
                .filter(item -> "sold".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList()));
        _pausedListings.postValue(allItems.stream()
                .filter(item -> "paused".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList()));
    }

    private void loadUserReviews(String userId) {
        ratingRepository.getRatingsForUser(userId, 50, new Callback<List<Rating>>() {
            @Override
            public void onSuccess(List<Rating> ratings) {
                _reviews.postValue(ratings != null ? ratings : Collections.emptyList());
                if (loadedUser != null && ratings != null) {
                    loadedUser.setTotalRatingCount(ratings.size());
                    combineAndPostState();
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load reviews", e);
            }
        });
    }

    public void updateSelectedItemStatus(String newStatus) {
        Item itemToUpdate = _selectedItem.getValue();
        if (itemToUpdate == null) {
            _toastMessage.setValue(new Event<>("No item selected."));
            return;
        }
        _isLoading.setValue(true);
        itemRepository.updateItemStatus(itemToUpdate.getItemId(), newStatus, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Listing status updated successfully!"));
                loadAllData(); // Tải lại toàn bộ dữ liệu để đồng bộ
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Error updating status: " + e.getMessage()));
            }
        });
    }

    public void deleteSelectedItem() {
        Item itemToDelete = _selectedItem.getValue();
        if (itemToDelete == null) {
            _toastMessage.setValue(new Event<>("No item selected."));
            return;
        }
        _isLoading.setValue(true);
        itemRepository.deleteItem(itemToDelete.getItemId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Listing deleted successfully!"));
                loadAllData(); // Tải lại
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Error deleting listing: " + e.getMessage()));
            }
        });
    }

    public void onReportReview(Rating rating) {
        _reportReviewEvent.setValue(new Event<>(rating));
    }
}