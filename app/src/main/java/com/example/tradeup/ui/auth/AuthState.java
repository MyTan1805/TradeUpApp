package com.example.tradeup.ui.auth;

import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;

public abstract class AuthState {
    private AuthState() {} // Private constructor

    public static final class Idle extends AuthState {
        public Idle() {}
    }

    public static final class Loading extends AuthState {
        public Loading() {}
    }

    public static final class Success extends AuthState {
        @Nullable // User có thể null trong một số trường hợp thành công nhất định (ví dụ: gửi email reset pass)
        public final FirebaseUser user;
        @Nullable
        public final String message;

        public Success(@Nullable FirebaseUser user, @Nullable String message) {
            this.user = user;
            this.message = message;
        }
        public Success(@Nullable FirebaseUser user) {
            this(user, null);
        }
    }

    public static final class Error extends AuthState {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}