package com.example.tradeup.core.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("TradeUpAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_EMAIL = "user_email"
        const val REMEMBER_ME = "remember_me"
    }

    fun saveEmail(email: String) {
        val editor = prefs.edit()
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun getEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun clearEmail() {
        val editor = prefs.edit()
        editor.remove(USER_EMAIL)
        editor.apply()
    }

    fun setRememberMe(remember: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(REMEMBER_ME, remember)
        editor.apply()
    }

    fun shouldRememberMe(): Boolean {
        return prefs.getBoolean(REMEMBER_ME, false)
    }
}