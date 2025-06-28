package com.example.tradeup.ui.listing;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.data.model.Item;
// import com.example.tradeup.data.repository.ItemRepository; // Sẽ dùng sau

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MyListingsViewModel extends ViewModel {

    // private final ItemRepository itemRepository; // Sẽ dùng sau

    // Dùng LiveData để chứa danh sách sản phẩm
    private final MutableLiveData<List<Item>> _allMyListings = new MutableLiveData<>();

    private final MutableLiveData<List<Item>> _activeListings = new MutableLiveData<>();
    public LiveData<List<Item>> getActiveListings() { return _activeListings; }

    private final MutableLiveData<List<Item>> _soldListings = new MutableLiveData<>();
    public LiveData<List<Item>> getSoldListings() { return _soldListings; }

    private final MutableLiveData<List<Item>> _pausedListings = new MutableLiveData<>();
    public LiveData<List<Item>> getPausedListings() { return _pausedListings; }


    @Inject
    public MyListingsViewModel(/* ItemRepository itemRepository */) { // Sẽ inject repository sau
        // this.itemRepository = itemRepository;
        loadMyListings(); // Tải dữ liệu khi ViewModel được tạo
    }

    public void loadMyListings() {
        // === TẠM THỜI DÙNG DỮ LIỆU GIẢ ĐỂ TEST ===
        List<Item> sampleItems = createSampleData();
        _allMyListings.postValue(sampleItems);

        // Phân loại dữ liệu giả vào các LiveData tương ứng
        filterListings();
    }

    private void filterListings() {
        List<Item> allItems = _allMyListings.getValue();
        if (allItems == null) return;

        // Dùng Stream API để lọc
        _activeListings.postValue(allItems.stream()
                .filter(item -> "active".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList()));

        _soldListings.postValue(allItems.stream()
                .filter(item -> "sold".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList()));

        _pausedListings.postValue(allItems.stream()
                .filter(item -> "paused".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toList()));
    }

    // Hàm tạo dữ liệu giả để test giao diện
    private List<Item> createSampleData() {
        List<Item> items = new ArrayList<>();

        // Item 1 (Active)
        Item item1 = new Item(); // Dùng constructor rỗng
        item1.setItemId("item1"); // Dùng setter để gán giá trị
        item1.setTitle("Vintage Leather Jacket");
        item1.setPrice(125.00);
        item1.setStatus("active");
        item1.setImageUrls(new ArrayList<>(List.of("https://via.placeholder.com/300/000000/FFFFFF?text=Jacket")));
        items.add(item1);

        // Item 2 (Active)
        Item item2 = new Item();
        item2.setItemId("item2");
        item2.setTitle("Nike Air Max 2023");
        item2.setPrice(89.99);
        item2.setStatus("active");
        item2.setImageUrls(new ArrayList<>(List.of("https://via.placeholder.com/300/FF0000/FFFFFF?text=Shoes")));
        items.add(item2);

        // Item 3 (Active)
        Item item3 = new Item();
        item3.setItemId("item3");
        item3.setTitle("iPhone 13 Pro Max");
        item3.setPrice(649.00);
        item3.setStatus("active");
        item3.setImageUrls(new ArrayList<>(List.of("https://via.placeholder.com/300/0000FF/FFFFFF?text=iPhone")));
        items.add(item3);

        // Item 4 (Sold)
        Item item4 = new Item();
        item4.setItemId("item4");
        item4.setTitle("Gaming Console");
        item4.setPrice(299.99);
        item4.setStatus("sold");
        item4.setImageUrls(new ArrayList<>(List.of("https://via.placeholder.com/300/FFFFFF/000000?text=Console")));
        items.add(item4);

        // Item 5 (Paused)
        Item item5 = new Item();
        item5.setItemId("item5");
        item5.setTitle("Antique Watch");
        item5.setPrice(199.99);
        item5.setStatus("paused");
        item5.setImageUrls(new ArrayList<>(List.of("https://via.placeholder.com/300/808080/FFFFFF?text=Watch")));
        items.add(item5);

        return items;
    }

    // TODO: Sau này, bạn sẽ có một hàm gọi Repository thật
    /*
    public void fetchMyListingsFromRepo() {
        String currentUserId = ...;
        itemRepository.getItemsBySeller(currentUserId, result -> {
            if (result.isSuccess()) {
                _allMyListings.postValue(result.getData());
                filterListings();
            } else {
                // Xử lý lỗi
            }
        });
    }
    */
}