// File: src/main/java/com/example/tradeup/ui/details/ItemDetailViewState.java
package com.example.tradeup.ui.details;

import com.example.tradeup.data.model.Item;

// Lớp cha trừu tượng cho các trạng thái của màn hình chi tiết sản phẩm
public abstract class ItemDetailViewState {
    private ItemDetailViewState() {} // Ngăn không cho tạo instance trực tiếp

    // Trạng thái đang tải dữ liệu
    public static final class Loading extends ItemDetailViewState {}

    // Trạng thái tải dữ liệu thành công
    public static final class Success extends ItemDetailViewState {
        public final Item item;
        public final String categoryName;
        public final String conditionName;

        public Success(Item item, String categoryName, String conditionName) {
            this.item = item;
            this.categoryName = categoryName;
            this.conditionName = conditionName;
        }
    }

    // Trạng thái có lỗi xảy ra
    public static final class Error extends ItemDetailViewState {
        public final String message;
        public Error(String message) {
            this.message = message;
        }
    }
}