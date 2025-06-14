package com.example.tradeup.ui.profile;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Item; // Model Item (Java)
import com.example.tradeup.data.model.User; // Model User (Java)
import java.util.List;
import java.util.Collections; // Import Collections

public abstract class ProfileState {
    private ProfileState() {} // Private constructor

    public static final class Idle extends ProfileState {
        public Idle() {}
    }

    public static final class Loading extends ProfileState {
        public Loading() {}
    }

    public static final class Success extends ProfileState {
        @NonNull
        public final User user;
        @NonNull
        public final List<Item> items;
        public final boolean isCurrentUserProfile;

        public Success(@NonNull User user, @NonNull List<Item> items, boolean isCurrentUserProfile) {
            this.user = user;
            this.items = items != null ? items : Collections.emptyList(); // Đảm bảo items không null
            this.isCurrentUserProfile = isCurrentUserProfile;
        }
    }

    public static final class Error extends ProfileState {
        @NonNull
        public final String message;

        public Error(@NonNull String message) {
            this.message = message;
        }
    }
}