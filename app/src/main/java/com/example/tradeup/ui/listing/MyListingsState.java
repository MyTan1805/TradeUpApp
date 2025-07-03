// File: src/main/java/com/example/tradeup/ui/listing/MyListingsState.java
package com.example.tradeup.ui.listing;

import com.example.tradeup.data.model.Item;
import java.util.List;

// Dùng abstract class để mô phỏng Sealed Class trong Java
public abstract class MyListingsState {
    private MyListingsState() {}

    public static final class Loading extends MyListingsState {}

    public static final class Success extends MyListingsState {
        public final List<Item> activeItems;
        public final List<Item> soldItems;
        public final List<Item> pausedItems;
        public Success(List<Item> active, List<Item> sold, List<Item> paused) {
            this.activeItems = active;
            this.soldItems = sold;
            this.pausedItems = paused;
        }
    }

    public static final class Error extends MyListingsState {
        public final String message;
        public Error(String message) { this.message = message; }
    }
}