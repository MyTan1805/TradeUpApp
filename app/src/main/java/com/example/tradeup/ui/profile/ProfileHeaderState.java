// File: src/main/java/com/example/tradeup/ui/profile/ProfileHeaderState.java
package com.example.tradeup.ui.profile;

import com.example.tradeup.data.model.User;

public abstract class ProfileHeaderState {

    private ProfileHeaderState() {}

    public static final class Loading extends ProfileHeaderState {}

    public static final class Success extends ProfileHeaderState {
        public final User user;
        public final boolean isCurrentUserProfile;

        public Success(User user, boolean isCurrentUserProfile) {
            this.user = user;
            this.isCurrentUserProfile = isCurrentUserProfile;
        }
    }

    public static final class Error extends ProfileHeaderState {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}