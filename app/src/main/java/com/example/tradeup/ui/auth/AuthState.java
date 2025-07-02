// File: src/main/java/com/example/tradeup/ui/auth/AuthState.java
package com.example.tradeup.ui.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;

public abstract class AuthState {
    private AuthState() {} // Private constructor

    public static final class Idle extends AuthState {}
    public static final class Loading extends AuthState {}

    public static final class Success extends AuthState {
        public final FirebaseUser user;
        public final String message;

        public Success(@NonNull FirebaseUser user, @NonNull String message) {
            this.user = user;
            this.message = message;
        }
    }

    public static final class Error extends AuthState {
        public final String message;
        public Error(@NonNull String message) {
            this.message = message;
        }
    }
}