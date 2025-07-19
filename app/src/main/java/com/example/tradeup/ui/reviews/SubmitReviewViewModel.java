// File: src/main/java/com/example/tradeup/ui/reviews/SubmitReviewViewModel.java
package com.example.tradeup.ui.reviews;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Rating;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.RatingRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.ItemRepository;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SubmitReviewViewModel extends ViewModel {

    private final RatingRepository ratingRepository;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    // --- Dữ liệu nhận từ Navigation Args ---
    private final String transactionId;
    private final String ratedUserId; // Người được đánh giá
    private final String itemId;

    // --- LiveData cho UI ---

    private final MutableLiveData<Item> _item = new MutableLiveData<>();
    public LiveData<Item> getItem() { return _item; }
    private final MutableLiveData<User> _ratedUser = new MutableLiveData<>();
    public LiveData<User> getRatedUser() { return _ratedUser; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<Boolean>> _submitSuccess = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getSubmitSuccess() { return _submitSuccess; }


    @Inject
    public SubmitReviewViewModel(
            RatingRepository ratingRepository,
            AuthRepository authRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            SavedStateHandle savedStateHandle
    ) {
        this.ratingRepository = ratingRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;

        // Lấy dữ liệu được truyền qua
        this.transactionId = savedStateHandle.get("transactionId");
        this.ratedUserId = savedStateHandle.get("ratedUserId");
        this.itemId = savedStateHandle.get("itemId");

        loadRatedUserInfo();
        loadItemInfo();
    }

    private void loadItemInfo() {
        if (itemId == null) return;
        itemRepository.getItemById(itemId, new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item != null) {
                    _item.postValue(item);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to load item info."));
            }
        });
    }

    private void loadRatedUserInfo() {
        if (ratedUserId == null) return;
        // *** SỬA Ở ĐÂY: Chuyển sang dùng CompletableFuture ***
        userRepository.getUserProfile(ratedUserId)
                .whenComplete((user, throwable) -> {
                    if (throwable != null) {
                        _toastMessage.postValue(new Event<>("Failed to load user info."));
                    } else if (user != null) {
                        _ratedUser.postValue(user);
                    }
                });
    }

    public void submitReview(int stars, String feedbackText) {
        FirebaseUser rater = authRepository.getCurrentUser(); // Người đang đánh giá
        User ratedUser = _ratedUser.getValue();

        if (rater == null || ratedUserId == null || transactionId == null) {
            _toastMessage.setValue(new Event<>("Error: Missing required information."));
            return;
        }
        if (stars == 0) {
            _toastMessage.setValue(new Event<>("Please select a rating."));
            return;
        }

        _isLoading.setValue(true);

        Rating rating = new Rating();
        rating.setTransactionId(transactionId);
        rating.setItemId(itemId);
        rating.setRatedUserId(ratedUserId);
        rating.setRaterUserId(rater.getUid());
        rating.setRaterDisplayName(rater.getDisplayName() != null ? rater.getDisplayName() : "Anonymous");
        if (rater.getPhotoUrl() != null) {
            rating.setRaterProfilePictureUrl(rater.getPhotoUrl().toString());
        }
        rating.setStars(stars);
        rating.setFeedbackText(feedbackText);

        // Lưu ý: FirebaseRatingSource đã xử lý transaction để cập nhật điểm trung bình
        ratingRepository.submitRating(rating, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _isLoading.postValue(false);
                _submitSuccess.postValue(new Event<>(true));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Failed to submit review: " + e.getMessage()));
            }
        });
    }
}