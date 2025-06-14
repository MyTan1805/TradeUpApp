package com.example.tradeup.ui.profile;

import androidx.annotation.NonNull;

public abstract class UpdateProfileState {
    private UpdateProfileState() {} // Private constructor

    public static final class Idle extends UpdateProfileState {
        public Idle() {}
    }

    public static final class Loading extends UpdateProfileState {
        public Loading() {}
    }

    public static final class Success extends UpdateProfileState {
        private final String message; // Giữ private

        public Success(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        public String getMessage() { // Getter public
            return message;
        }
    }

    public static final class Error extends UpdateProfileState {
        private final String message; // Giữ private

        public Error(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        public String getMessage() { // Getter public
            return message;
        }
    }
}