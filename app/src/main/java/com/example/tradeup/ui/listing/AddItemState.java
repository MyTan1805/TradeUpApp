// package: com.example.tradeup.ui.listing
package com.example.tradeup.ui.listing;

// Lớp state không cần import gì cả
abstract class AddItemState {
    private AddItemState() {}

    static final class Idle extends AddItemState {}

    static final class Loading extends AddItemState {
        public final String message;
        Loading(String message) { this.message = message; }
    }

    static final class Success extends AddItemState {
        public final String itemId;
        Success(String itemId) { this.itemId = itemId; }
    }

    static final class Error extends AddItemState {
        public final String message;
        Error(String message) { this.message = message; }
    }
}