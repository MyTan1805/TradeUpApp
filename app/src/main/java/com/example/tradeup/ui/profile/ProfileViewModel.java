// File: src/main/java/com/example/tradeup/ui/profile/ProfileViewModel.java
package com.example.tradeup.ui.profile;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
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
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RatingRepository ratingRepository;
    private final TransactionRepository transactionRepository;

    private final MutableLiveData<ProfileHeaderState> _headerState = new MutableLiveData<>();
    public LiveData<ProfileHeaderState> getHeaderState() { return _headerState; }

    private final MutableLiveData<List<Item>> _activeListings = new MutableLiveData<>();
    public LiveData<List<Item>> getActiveListings() { return _activeListings; }

    private final MutableLiveData<List<Transaction>> _soldTransactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getSoldTransactions() { return _soldTransactions; }

    private final MutableLiveData<List<Rating>> _reviews = new MutableLiveData<>();
    public LiveData<List<Rating>> getReviews() { return _reviews; }

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
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.ratingRepository = ratingRepository;
        this.transactionRepository = transactionRepository;

        FirebaseUser fbUser = authRepository.getCurrentUser();
        this.currentAuthUserUid = (fbUser != null) ? fbUser.getUid() : null;
        this.profileUserIdArg = savedStateHandle.get("profileUserId");

        loadUserProfile();
    }

    public void loadUserProfile() {
        _headerState.setValue(new ProfileHeaderState.Loading());
        String targetUserId = (profileUserIdArg != null && !profileUserIdArg.trim().isEmpty()) ? profileUserIdArg : currentAuthUserUid;

        if (targetUserId == null) {
            _headerState.postValue(new ProfileHeaderState.Error("Không xác định được người dùng."));
            return;
        }

        userRepository.getUserProfile(targetUserId, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    _headerState.postValue(new ProfileHeaderState.Success(user, targetUserId.equals(currentAuthUserUid)));
                    loadDataForTabs(targetUserId);
                } else {
                    _headerState.postValue(new ProfileHeaderState.Error("Không tìm thấy người dùng."));
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _headerState.postValue(new ProfileHeaderState.Error("Lỗi tải hồ sơ: " + e.getMessage()));
            }
        });
    }

    private void loadDataForTabs(String userId) {
        loadUserActiveListings(userId);
        loadUserSoldItems(userId);
        loadUserReviews(userId);
    }

    private void loadUserActiveListings(String userId) {
        itemRepository.getItemsBySellerId(userId, new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                _activeListings.postValue(items != null ? items : Collections.emptyList());
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load active listings", e);
                _activeListings.postValue(Collections.emptyList());
            }
        });
    }

    private void loadUserSoldItems(String userId) {
        transactionRepository.getTransactionsByUser(userId, "sellerId", 50, new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                _soldTransactions.postValue(transactions != null ? transactions : Collections.emptyList());
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load sold items", e);
                _soldTransactions.postValue(Collections.emptyList());
            }
        });
    }

    private void loadUserReviews(String userId) {
        ratingRepository.getRatingsForUser(userId, 50, new Callback<List<Rating>>() {
            @Override
            public void onSuccess(List<Rating> ratings) {
                _reviews.postValue(ratings != null ? ratings : Collections.emptyList());
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load reviews", e);
                _reviews.postValue(Collections.emptyList());
            }
        });
    }
}