// File: src/main/java/com/example/tradeup/ui/listing/MyListingsNavigationEvent.java
package com.example.tradeup.ui.listing;

import com.example.tradeup.data.model.Item;

// Lớp cha cho các sự kiện điều hướng
public abstract class MyListingsNavigationEvent {
    private MyListingsNavigationEvent() {}

    public static final class ToEditItem extends MyListingsNavigationEvent {
        public final String itemId;
        public ToEditItem(String itemId) { this.itemId = itemId; }
    }

    public static final class ToRateBuyer extends MyListingsNavigationEvent {
        public final String transactionId;
        public final String ratedUserId; // Buyer ID
        public final String itemId;
        public ToRateBuyer(String transactionId, String ratedUserId, String itemId) {
            this.transactionId = transactionId;
            this.ratedUserId = ratedUserId;
            this.itemId = itemId;
        }
    }
}