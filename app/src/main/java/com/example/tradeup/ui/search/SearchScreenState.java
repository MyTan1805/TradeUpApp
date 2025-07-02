package com.example.tradeup.ui.search;

import com.example.tradeup.data.model.Item;
import java.util.List;

public abstract class SearchScreenState {
    private SearchScreenState() {}

    public static final class Idle extends SearchScreenState {}
    public static final class Loading extends SearchScreenState {}
    public static final class Empty extends SearchScreenState {}

    public static final class Success extends SearchScreenState {
        public final List<Item> items;
        public Success(List<Item> items) { this.items = items; }
    }

    public static final class Error extends SearchScreenState {
        public final String message;
        public Error(String message) { this.message = message; }
    }
}