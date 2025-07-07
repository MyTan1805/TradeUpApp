// File: src/main/java/com/example/tradeup/ui/home/HomeState.java
package com.example.tradeup.ui.home;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import java.util.List;

// Sử dụng abstract class để mô phỏng sealed class trong Java
public abstract class HomeState {
    private HomeState() {} // Ngăn không cho tạo instance trực tiếp

    // Trạng thái đang tải lần đầu
    public static final class Loading extends HomeState {}

    // Trạng thái tải thành công, chứa tất cả dữ liệu cần thiết
    public static final class Success extends HomeState {
        @NonNull public final List<DisplayCategoryConfig> categories;
        @NonNull public final List<Item> recentItems;

        public Success(@NonNull List<DisplayCategoryConfig> categories, @NonNull List<Item> recentItems) {
            this.categories = categories;
            this.recentItems = recentItems;
        }
    }

    // Trạng thái khi không có dữ liệu nào
    public static final class Empty extends HomeState {
        @NonNull public final List<DisplayCategoryConfig> categories; // Vẫn cần categories để hiển thị

        public Empty(@NonNull List<DisplayCategoryConfig> categories) {
            this.categories = categories;
        }
    }

    // Trạng thái khi có lỗi xảy ra
    public static final class Error extends HomeState {
        @NonNull public final String message;
        public Error(@NonNull String message) { this.message = message; }
    }
}