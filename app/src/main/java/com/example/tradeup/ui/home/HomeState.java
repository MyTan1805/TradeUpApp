// File: src/main/java/com/example/tradeup/ui/home/HomeState.java
package com.example.tradeup.ui.home;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.CategoryConfig;
import java.util.List;

public abstract class HomeState {
    private HomeState() {}

    public static final class Loading extends HomeState {}

    public static final class Success extends HomeState {
        @NonNull public final List<CategoryConfig> categories;
        @NonNull public final List<Item> recentItems;

        public Success(@NonNull List<CategoryConfig> categories, @NonNull List<Item> recentItems) {
            this.categories = categories;
            this.recentItems = recentItems;
        }
    }

    public static final class Empty extends HomeState {
        @NonNull public final List<CategoryConfig> categories;
        public Empty(@NonNull List<CategoryConfig> categories) {
            this.categories = categories;
        }
    }

    public static final class Error extends HomeState {
        @NonNull public final String message;
        public Error(@NonNull String message) { this.message = message; }
    }
}