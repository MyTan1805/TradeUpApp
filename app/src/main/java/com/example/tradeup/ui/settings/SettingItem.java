package com.example.tradeup.ui.settings;

import androidx.annotation.DrawableRes;

/**
 * Lớp cha trừu tượng đại diện cho một mục trong màn hình Cài đặt.
 */
public abstract class SettingItem {

    /**
     * Item dạng tiêu đề nhóm (vd: "ACCOUNT MANAGEMENT")
     */
    public static class GroupHeader extends SettingItem {
        public final String title;
        public GroupHeader(String title) { this.title = title; }
    }

    /**
     * Item dạng điều hướng, có icon, tiêu đề và mũi tên (vd: "Edit Profile")
     */
    public static class Navigation extends SettingItem {
        @DrawableRes public final int iconResId;
        public final String title;
        public final String tag; // Dùng để xác định item nào được nhấn
        public final int textColor; // 0 nếu dùng màu mặc định
        public final int iconTint; // 0 nếu dùng màu mặc định


        public Navigation(String tag, @DrawableRes int iconResId, String title) {
            this(tag, iconResId, title, 0, 0);
        }

        public Navigation(String tag, @DrawableRes int iconResId, String title, int textColor, int iconTint) {
            this.tag = tag;
            this.iconResId = iconResId;
            this.title = title;
            this.textColor = textColor;
            this.iconTint = iconTint;
        }
    }

    /**
     * Item dạng công tắc bật/tắt (vd: "New Messages")
     */
    public static class Switch extends SettingItem {
        public final String title;
        public boolean isEnabled; // Trạng thái hiện tại của công tắc
        public final String tag; // Dùng để xác định công tắc nào được thay đổi

        public Switch(String tag, String title, boolean isEnabled) {
            this.tag = tag;
            this.title = title;
            this.isEnabled = isEnabled;
        }
    }

    /**
     * Item dạng thông tin, không thể nhấn, có thể có giá trị bên phải (vd: "About TradeUp")
     */
    public static class Info extends SettingItem {
        @DrawableRes public final int iconResId;
        public final String title;
        public final String value;

        public Info(@DrawableRes int iconResId, String title, String value) {
            this.iconResId = iconResId;
            this.title = title;
            this.value = value;
        }
    }

    /**
     * Item đặc biệt cho nút Đăng xuất
     */
    public static class Logout extends SettingItem {}
}