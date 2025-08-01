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

@HiltViewModel
public class MyListingsViewModel extends ViewModel {

    private static final String TAG = "MyListingsViewModel";

    private final ItemRepository itemRepository;
    private final AuthRepository authRepository;
    private final TransactionRepository transactionRepository;

    private final MutableLiveData<Item> _selectedItem = new MutableLiveData<>();

    public LiveData<Item> getSelectedItemLiveData() { // << ĐỔI TÊN HÀM GETTER
        return _selectedItem;
    }
    private final MutableLiveData<Event<Boolean>> _openMenuEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getOpenMenuEvent() { return _openMenuEvent; }

    private final MutableLiveData<MyListingsState> _state = new MutableLiveData<>();
    public LiveData<MyListingsState> getState() { return _state; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<Event<MyListingsNavigationEvent>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<MyListingsNavigationEvent>> getNavigationEvent() { return _navigationEvent; }

    // Biến private để lưu trữ item đang được chọn
    private Item selectedItem;

    public void setSelectedItem(Item item) {
        _selectedItem.setValue(item);
    }

    public void onMenuItemClicked(Item item) {
        setSelectedItem(item); // Lưu lại item được chọn
        _openMenuEvent.setValue(new Event<>(true)); // Bắn sự kiện để Fragment cha mở dialog
    }

    // Hàm này để lấy giá trị tức thời, có thể giữ lại nếu cần ở nơi khác
    public Item getSelectedItem() {
        return _selectedItem.getValue();
    }

    @Inject
    public MyListingsViewModel(ItemRepository itemRepository, AuthRepository authRepository, TransactionRepository transactionRepository) {
        this.itemRepository = itemRepository;
        this.authRepository = authRepository;
        this.transactionRepository = transactionRepository;
        loadMyListings();
    }

    public void loadMyListings() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            _state.setValue(new MyListingsState.Error("User not logged in."));
            return;
        }
        _state.setValue(new MyListingsState.Loading());

        itemRepository.getItemsBySellerId(currentUser.getUid(), new Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                if (items == null) {
                    items = Collections.emptyList();
                }
                List<Item> activeItems = items.stream().filter(i -> "available".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
                List<Item> soldItems = items.stream().filter(i -> "sold".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
                List<Item> pausedItems = items.stream().filter(i -> "paused".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());

                _state.setValue(new MyListingsState.Success(activeItems, soldItems, pausedItems));
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to load listings", e);
                _state.setValue(new MyListingsState.Error("Failed to load your listings."));
            }
        });
    }

    public void updateSelectedItemStatus(String newStatus) {
        Item itemToUpdate = _selectedItem.getValue();
        if (itemToUpdate == null) {
            _toastMessage.postValue(new Event<>("Please select an item first."));
            return;
        }
        _state.setValue(new MyListingsState.Loading()); // Hiển thị loading
        itemRepository.updateItemStatus(itemToUpdate.getItemId(), newStatus, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Listing status updated!"));
                loadMyListings(); // Tải lại toàn bộ danh sách để cập nhật giao diện
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Error updating status: " + e.getMessage()));
                loadMyListings(); // Vẫn tải lại để thoát khỏi trạng thái loading
            }
        });
    }

    public void deleteSelectedItem() {
        Item itemToDelete = _selectedItem.getValue();
        if (itemToDelete == null) {
            _toastMessage.postValue(new Event<>("Please select an item first."));
            return;
        }
        _state.setValue(new MyListingsState.Loading()); // Hiển thị loading
        itemRepository.deleteItem(itemToDelete.getItemId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Listing deleted."));
                loadMyListings(); // Tải lại
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Error deleting listing: " + e.getMessage()));
                loadMyListings(); // Vẫn tải lại
            }
        });
    }

    public void onEditOptionClicked() {
        Item item = _selectedItem.getValue();
        if (item != null) {
            _navigationEvent.setValue(new Event<>(new MyListingsNavigationEvent.ToEditItem(item.getItemId())));
        } else {
            _toastMessage.postValue(new Event<>("Cannot edit. Item data is missing."));
        }
    }

    public void onRateBuyerClicked(Item soldItem) {
        if (soldItem == null) return;
        _state.setValue(new MyListingsState.Loading());
        transactionRepository.getTransactionByItemId(soldItem.getItemId(), new Callback<Transaction>() {
            @Override
            public void onSuccess(Transaction transaction) {
                // Tải lại để tắt loading indicator, bất kể kết quả thế nào
                loadMyListings();
                if (transaction != null) {
                    _navigationEvent.setValue(new Event<>(new MyListingsNavigationEvent.ToRateBuyer(
                            transaction.getTransactionId(),
                            transaction.getBuyerId(),
                            soldItem.getItemId()
                    )));
                } else {
                    _toastMessage.setValue(new Event<>("Transaction info not found for this item."));
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                loadMyListings();
                _toastMessage.setValue(new Event<>("Error finding transaction."));
            }
        });
    }
}