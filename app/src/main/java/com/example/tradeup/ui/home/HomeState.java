package com.example.tradeup.ui.home;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.CategoryConfig;
import java.util.List;
import java.util.Set;

public abstract class HomeState {
    private HomeState() {}

    // Trạng thái đang tải
    public static final class Loading extends HomeState {}

    // Trạng thái thành công (CHỈ CÓ MỘT PHIÊN BẢN NÀY)
    public static final class Success extends HomeState {
        @NonNull public final List<CategoryConfig> categories;
        @NonNull public final List<Item> recentItems;
        @NonNull public final Set<String> savedItemIds;

        public Success(@NonNull List<CategoryConfig> categories, @NonNull List<Item> recentItems, @NonNull Set<String> savedItemIds) {
            this.categories = categories;
            this.recentItems = recentItems;
            this.savedItemIds = savedItemIds;
        }
    }

    // Trạng thái rỗng
    public static final class Empty extends HomeState {
        @NonNull public final List<CategoryConfig> categories;
        public Empty(@NonNull List<CategoryConfig> categories) {
            this.categories = categories;
        }
    }

    // Trạng thái lỗi
    public static final class Error extends HomeState {
        @NonNull public final String message;
        public Error(@NonNull String message) { this.message = message; }
    }
}