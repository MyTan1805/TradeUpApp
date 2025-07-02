package com.example.tradeup.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

// << FIX: Sử dụng Hilt để quản lý như một Singleton >>
@Singleton
public class SessionManager {

    private static final String PREF_NAME = "TradeUpAppPrefs";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_REMEMBER_ME = "remember_me";

    private final SharedPreferences prefs;

    @Inject
    public SessionManager(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public void setRememberMe(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
    }

    public boolean shouldRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}