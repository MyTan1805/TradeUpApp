package com.example.tradeup.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "TradeUpAppPrefs"; // Tên file SharedPreferences
    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;

    // Khai báo các key dưới dạng public static final String (tương tự companion object const val)
    public static final String USER_EMAIL = "user_email";
    public static final String REMEMBER_ME = "remember_me";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // Constructor nhận Context
    public SessionManager(Context context) {
        if (context != null) {
            prefs = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = prefs.edit(); // Khởi tạo editor một lần để tái sử dụng
        } else {
            // Xử lý trường hợp context là null nếu cần, ví dụ ném Exception
            // hoặc log lỗi, mặc dù với Hilt @ApplicationContext thì context thường không null.
            throw new IllegalArgumentException("Context cannot be null in SessionManager constructor");
        }
    }

    /**
     * Lưu địa chỉ email của người dùng.
     * @param email Email cần lưu.
     */
    public void saveEmail(String email) {
        if (editor != null) {
            editor.putString(USER_EMAIL, email);
            editor.apply(); // Hoặc editor.commit() nếu bạn cần biết kết quả ngay lập tức
        }
    }

    /**
     * Lấy địa chỉ email đã lưu.
     * @return Email đã lưu, hoặc null nếu chưa có.
     */
    public String getEmail() {
        if (prefs != null) {
            return prefs.getString(USER_EMAIL, null);
        }
        return null;
    }

    /**
     * Xóa địa chỉ email đã lưu.
     */
    public void clearEmail() {
        if (editor != null) {
            editor.remove(USER_EMAIL);
            editor.apply();
        }
    }

    /**
     * Thiết lập tùy chọn "Ghi nhớ tôi".
     * @param remember true nếu muốn ghi nhớ, false nếu không.
     */
    public void setRememberMe(boolean remember) {
        if (editor != null) {
            editor.putBoolean(REMEMBER_ME, remember);
            editor.apply();
        }
    }

    /**
     * Kiểm tra xem tùy chọn "Ghi nhớ tôi" có được bật không.
     * @return true nếu người dùng muốn ghi nhớ, false nếu không (mặc định là false).
     */
    public boolean shouldRememberMe() {
        if (prefs != null) {
            return prefs.getBoolean(REMEMBER_ME, false);
        }
        return false; // Trả về false nếu prefs chưa được khởi tạo
    }

    /**
     * (Tùy chọn) Xóa tất cả dữ liệu trong SharedPreferences này.
     * Hữu ích khi người dùng đăng xuất hoàn toàn và không muốn ghi nhớ gì.
     */
    public void clearSession() {
        if (editor != null) {
            editor.clear();
            editor.apply();
        }
    }
}