package com.example.tradeup.ui.listing;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.TransactionRepository;
import com.google.firebase.auth.FirebaseUser;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import com.example.tradeup.core.utils.Event;

@HiltViewModel
public class MyListingsViewModel extends ViewModel {

    private static final String TAG = "MyListingsViewModel";

    private final ItemRepository itemRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<Transaction>> _soldTransactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getSoldTransactions() { return _soldTransactions; }

    private final MutableLiveData<Event<Item>> _navigateToEditEvent = new MutableLiveData<>();
    public LiveData<Event<Item>> getNavigateToEditEvent() { return _navigateToEditEvent; }

    // LiveData cho các trạng thái và dữ liệu
    private final MutableLiveData<List<Item>> _activeListings = new MutableLiveData<>();
    public LiveData<List<Item>> getActiveListings() { return _activeListings; }

    private final MutableLiveData<List<Item>> _soldListings = new MutableLiveData<>();
    public LiveData<List<Item>> getSoldListings() { return _soldListings; }

    private final MutableLiveData<List<Item>> _pausedListings = new MutableLiveData<>();
    public LiveData<List<Item>> getPausedListings() { return _pausedListings; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    // LiveData để lưu item đang được chọn khi người dùng nhấn nút menu
    private final MutableLiveData<Item> _selectedItem = new MutableLiveData<>();
    public LiveData<Item> getSelectedItem() { return _selectedItem; }
    public void setSelectedItem(Item item) {
        _selectedItem.setValue(item);
    }

    @Inject
    public MyListingsViewModel(ItemRepository itemRepository, AuthRepository authRepository,
    TransactionRepository transactionRepository) {
        this.itemRepository = itemRepository;
        this.authRepository = authRepository;

        // Tải dữ liệu ngay khi ViewModel được tạo
        loadMyListings();
    }

    /**
     * Tải tất cả sản phẩm của người dùng hiện tại từ repository.
     */
    public void loadMyListings() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _toastMessage.postValue(new Event<>("User not logged in."));
            return;
        }
        _isLoading.postValue(true);
        itemRepository.getItemsBySellerId(currentUser.getUid(), new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                // Sau khi có dữ liệu, phân loại chúng vào các LiveData tương ứng
                filterAndPostListings(items != null ? items : Collections.emptyList());
                _isLoading.postValue(false);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load listings", e);
                _toastMessage.postValue(new Event<>("Failed to load your listings: " + e.getMessage()));
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Phân loại một danh sách item vào các LiveData 'active', 'sold', 'paused'.
     */
    private void filterAndPostListings(List<Item> allItems) {
        _activeListings.postValue(allItems.stream()
                .filter(i -> "available".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList()));
        _soldListings.postValue(allItems.stream()
                .filter(i -> "sold".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList()));
        _pausedListings.postValue(allItems.stream()
                .filter(i -> "paused".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList()));
    }

    /**
     * Cập nhật trạng thái của item đã được chọn.
     * @param newStatus Trạng thái mới ("sold", "paused", "available").
     */
    public void updateSelectedItemStatus(String newStatus) {
        Item itemToUpdate = _selectedItem.getValue();
        if (itemToUpdate == null) {
            _toastMessage.setValue(new Event<>("Error: No item selected."));
            return;
        }
        _isLoading.setValue(true);
        itemRepository.updateItemStatus(itemToUpdate.getItemId(), newStatus, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Listing status updated!"));
                loadMyListings(); // Tải lại toàn bộ danh sách để cập nhật giao diện
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Error updating status: " + e.getMessage()));
            }
        });
    }

    /**
     * Xóa item đã được chọn.
     */
    public void deleteSelectedItem() {
        Item itemToDelete = _selectedItem.getValue();
        if (itemToDelete == null) {
            _toastMessage.setValue(new Event<>("Error: No item selected."));
            return;
        }
        _isLoading.setValue(true);
        itemRepository.deleteItem(itemToDelete.getItemId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Listing deleted."));
                loadMyListings(); // Tải lại danh sách
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Error deleting listing: " + e.getMessage()));
            }
        });
    }
    public void onEditOptionClicked() {
        Item itemToEdit = _selectedItem.getValue();
        if (itemToEdit != null) {
            _navigateToEditEvent.setValue(new Event<>(itemToEdit));
        }
    }
}