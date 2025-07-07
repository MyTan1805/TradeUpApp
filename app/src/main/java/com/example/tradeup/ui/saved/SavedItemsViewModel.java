package com.example.tradeup.ui.saved;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.UserSavedItemsRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.Comparator;

@HiltViewModel
public class SavedItemsViewModel extends ViewModel {

    private static final String TAG = "SavedItemsViewModel";

    private final ItemRepository itemRepository;
    private final UserSavedItemsRepository savedItemsRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<List<Item>> _savedItems = new MutableLiveData<>();
    public LiveData<List<Item>> getSavedItems() { return _savedItems; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

    private final String currentUserId;

    public enum SortMode {
        DEFAULT, // Sắp xếp mặc định (có thể là theo ngày lưu mới nhất)
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW
    }

    private final MutableLiveData<SortMode> _sortMode = new MutableLiveData<>(SortMode.DEFAULT);

    // ...

    // << THÊM HÀM NÀY ĐỂ UI GỌI >>
    public void setSortMode(SortMode newMode) {
        if (_sortMode.getValue() == newMode) return; // Không sắp xếp lại nếu không có gì thay đổi

        _sortMode.setValue(newMode);
        List<Item> currentList = _savedItems.getValue();
        if (currentList == null || currentList.isEmpty()) return;

        ArrayList<Item> sortedList = new ArrayList<>(currentList);

        switch (newMode) {
            case PRICE_LOW_TO_HIGH:
                Collections.sort(sortedList, Comparator.comparingDouble(Item::getPrice));
                break;
            case PRICE_HIGH_TO_LOW:
                Collections.sort(sortedList, Comparator.comparingDouble(Item::getPrice).reversed());
                break;
            case DEFAULT:
            default:
                // Sắp xếp theo ngày tạo sản phẩm mới nhất, vì ta chưa lưu "ngày save"
                Collections.sort(sortedList, (i1, i2) -> {
                    if (i1.getCreatedAt() == null || i2.getCreatedAt() == null) return 0;
                    return i2.getCreatedAt().compareTo(i1.getCreatedAt());
                });
                break;
        }
        _savedItems.setValue(sortedList);
    }

    @Inject
    public SavedItemsViewModel(ItemRepository itemRepository, UserSavedItemsRepository savedItemsRepository, AuthRepository authRepository) {
        this.itemRepository = itemRepository;
        this.savedItemsRepository = savedItemsRepository;
        this.authRepository = authRepository;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = (user != null) ? user.getUid() : null;

        loadSavedItems();
    }

    public void loadSavedItems() {
        if (currentUserId == null) {
            _toastMessage.setValue(new Event<>("Please log in to see your saved items."));
            _savedItems.setValue(Collections.emptyList());
            return;
        }

        _isLoading.setValue(true);

        // Bước 1: Lấy danh sách ID các item đã lưu
        savedItemsRepository.getSavedItemIds(currentUserId, new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> itemIds) {
                if (itemIds == null || itemIds.isEmpty()) {
                    _savedItems.postValue(Collections.emptyList());
                    _isLoading.postValue(false);
                } else {
                    // Bước 2: Lấy thông tin chi tiết cho từng ID
                    fetchItemsDetails(itemIds);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Error loading saved items list."));
                Log.e(TAG, "Failed to get saved item IDs", e);
            }
        });
    }

    private void fetchItemsDetails(List<String> itemIds) {
        final List<Item> detailedItems = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger counter = new AtomicInteger(0);
        final int totalIds = itemIds.size();

        for (String id : itemIds) {
            itemRepository.getItemById(id, new Callback<Item>() {
                @Override
                public void onSuccess(Item item) {
                    if (item != null) {
                        // Chỉ thêm các item còn tồn tại
                        detailedItems.add(item);
                    }
                    // Kiểm tra xem đã fetch xong tất cả chưa
                    if (counter.incrementAndGet() == totalIds) {
                        _savedItems.postValue(detailedItems);
                        _isLoading.postValue(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Failed to fetch detail for item ID: " + id, e);
                    // Vẫn tiếp tục dù một item bị lỗi
                    if (counter.incrementAndGet() == totalIds) {
                        _savedItems.postValue(detailedItems);
                        _isLoading.postValue(false);
                    }
                }
            });
        }
    }

    public void unsaveItem(Item item) {
        if (currentUserId == null || item == null) return;

        savedItemsRepository.unsaveItem(currentUserId, item.getItemId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>(item.getTitle() + " has been unsaved."));
                // Tải lại danh sách để cập nhật UI
                loadSavedItems();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Failed to unsave item."));
            }
        });
    }

    // TODO: Thêm logic cho các hàm filter
    public void onSortByDate() {
        // Sắp xếp danh sách _savedItems hiện tại theo ngày lưu (cần thêm trường ngày lưu)
    }

    public void onSortByPrice() {
        // Sắp xếp danh sách _savedItems hiện tại theo giá
    }
}