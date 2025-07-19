// File: src/main/java/com/example/tradeup/core/utils/UserRoleManager.java
package com.example.tradeup.core.utils;

import javax.inject.Inject;
// import javax.inject.Singleton; // <-- XÓA DÒNG NÀY

// @Singleton // <-- XÓA DÒNG NÀY
public class UserRoleManager {
    private boolean isAdmin = false;

    @Inject
    public UserRoleManager() {}

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void clear() {
        isAdmin = false;
    }
}