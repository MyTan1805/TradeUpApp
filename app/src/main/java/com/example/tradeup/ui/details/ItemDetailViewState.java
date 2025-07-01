// File: src/main/java/com/example/tradeup/ui/details/ItemDetailViewState.java

package com.example.tradeup.ui.details;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;

/**
 * Lớp sealed/abstract để quản lý các trạng thái của màn hình chi tiết sản phẩm.
 */
public abstract class ItemDetailViewState {
    private ItemDetailViewState() {} // Ngăn không cho tạo instance trực tiếp

    /**
     * Trạng thái đang tải dữ liệu. UI sẽ hiển thị một ProgressBar.
     */
    public static final class Loading extends ItemDetailViewState {}

    /**
     * Trạng thái tải dữ liệu thành công, chứa tất cả dữ liệu cần thiết để hiển thị.
     */
    public static final class Success extends ItemDetailViewState {
        @NonNull public final Item item;
        @NonNull public final User seller; // Thông tin người bán
        @NonNull public final String categoryName;
        @NonNull public final String conditionName;
        public final boolean isBookmarked; // Thêm field này

        // *** ĐẢM BẢO CONSTRUCTOR NÀY CÓ 5 THAM SỐ ***
        public Success(@NonNull Item item, @NonNull User seller, @NonNull String categoryName, @NonNull String conditionName, boolean isBookmarked) {
            this.item = item;
            this.seller = seller;
            this.categoryName = categoryName;
            this.conditionName = conditionName;
            this.isBookmarked = isBookmarked; // Gán giá trị
        }
    }

    /**
     * Trạng thái có lỗi xảy ra. UI sẽ hiển thị một thông báo lỗi.
     */
    public static final class Error extends ItemDetailViewState {
        @NonNull public final String message;

        public Error(@NonNull String message) {
            this.message = message;
        }
    }
}