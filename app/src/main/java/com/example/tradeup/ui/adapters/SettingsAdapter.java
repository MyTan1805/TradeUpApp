package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tradeup.R;
import com.example.tradeup.ui.settings.SettingItem;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Định nghĩa các hằng số cho từng loại ViewType
    private static final int TYPE_GROUP_HEADER = 0;
    private static final int TYPE_NAVIGATION = 1;
    private static final int TYPE_SWITCH = 2;
    private static final int TYPE_INFO = 3;
    private static final int TYPE_LOGOUT = 4;

    private final List<SettingItem> items;
    private final OnSettingItemClickListener listener;

    // Interface để fragment lắng nghe sự kiện click
    public interface OnSettingItemClickListener {
        void onNavigationItemClick(String tag);
        void onSwitchItemChanged(String tag, boolean isChecked);
    }

    public SettingsAdapter(List<SettingItem> items, OnSettingItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        SettingItem item = items.get(position);
        if (item instanceof SettingItem.GroupHeader) return TYPE_GROUP_HEADER;
        if (item instanceof SettingItem.Navigation) return TYPE_NAVIGATION;
        if (item instanceof SettingItem.Switch) return TYPE_SWITCH;
        if (item instanceof SettingItem.Info) return TYPE_INFO;
        if (item instanceof SettingItem.Logout) return TYPE_LOGOUT;
        return -1; // Trường hợp không xác định
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_GROUP_HEADER:
                View headerView = inflater.inflate(R.layout.item_setting_group_header, parent, false);
                return new GroupHeaderViewHolder(headerView);
            case TYPE_SWITCH:
                View switchView = inflater.inflate(R.layout.item_setting_switch, parent, false);
                return new SwitchViewHolder(switchView, listener);
            case TYPE_LOGOUT:
                View logoutView = inflater.inflate(R.layout.item_setting_logout, parent, false);
                return new LogoutViewHolder(logoutView, listener);
            case TYPE_INFO:
            case TYPE_NAVIGATION:
            default:
                View navView = inflater.inflate(R.layout.item_setting_navigation, parent, false);
                return new NavigationViewHolder(navView, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = items.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_GROUP_HEADER:
                ((GroupHeaderViewHolder) holder).bind((SettingItem.GroupHeader) item);
                break;
            case TYPE_NAVIGATION:
                ((NavigationViewHolder) holder).bind((SettingItem.Navigation) item);
                break;
            case TYPE_SWITCH:
                ((SwitchViewHolder) holder).bind((SettingItem.Switch) item);
                break;
            case TYPE_INFO:
                ((NavigationViewHolder) holder).bindInfo((SettingItem.Info) item);
                break;
            case TYPE_LOGOUT:
                // Không cần bind gì cả, listener đã được gán trong onCreateViewHolder
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- CÁC LỚP VIEWHOLDER ---

    static class GroupHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        GroupHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView;
        }
        void bind(SettingItem.GroupHeader item) {
            title.setText(item.title);
        }
    }

    static class NavigationViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView value;
        ImageView arrow;
        OnSettingItemClickListener listener;
        Context context;
        private final int defaultTextColor;
        private final int defaultIconColor;


        NavigationViewHolder(@NonNull View itemView, OnSettingItemClickListener listener) {
            super(itemView);
            this.context = itemView.getContext();
            this.listener = listener;
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            value = itemView.findViewById(R.id.value);
            arrow = itemView.findViewById(R.id.arrow);

            // Lấy màu mặc định từ theme để reset
            defaultTextColor = getThemeColor(context, android.R.attr.textColorPrimary);
            defaultIconColor = getThemeColor(context, android.R.attr.textColorSecondary);
        }

        void bind(SettingItem.Navigation item) {
            // Thiết lập trạng thái ban đầu cho item điều hướng
            title.setText(item.title);
            value.setVisibility(View.GONE);
            arrow.setVisibility(View.VISIBLE);
            itemView.setClickable(true);
            itemView.setOnClickListener(v -> listener.onNavigationItemClick(item.tag));

            // Xử lý icon
            if (item.iconResId != 0) {
                icon.setImageResource(item.iconResId);
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }

            // Xử lý màu chữ (reset về mặc định nếu không có màu đặc biệt)
            if (item.textColor != 0) {
                title.setTextColor(ContextCompat.getColor(context, item.textColor));
            } else {
                title.setTextColor(defaultTextColor);
            }

            // Xử lý màu icon (reset về mặc định nếu không có màu đặc biệt)
            if (item.iconTint != 0) {
                icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.iconTint)));
            } else {
                icon.setImageTintList(ColorStateList.valueOf(defaultIconColor));
            }
        }

        void bindInfo(SettingItem.Info item) {
            // Thiết lập trạng thái cho item thông tin
            title.setText(item.title);
            value.setText(item.value);
            value.setVisibility(View.VISIBLE);
            arrow.setVisibility(View.GONE);
            itemView.setClickable(false);
            itemView.setOnClickListener(null);

            if (item.iconResId != 0) {
                icon.setImageResource(item.iconResId);
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }

            // Reset màu sắc về mặc định
            title.setTextColor(defaultTextColor);
            icon.setImageTintList(ColorStateList.valueOf(defaultIconColor));
        }

        // Hàm tiện ích để lấy màu từ theme attribute
        private int getThemeColor(Context context, @AttrRes int attr) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(attr, typedValue, true);
            return typedValue.data;
        }
    }

    static class SwitchViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        SwitchMaterial switchCompat;
        OnSettingItemClickListener listener;

        SwitchViewHolder(@NonNull View itemView, OnSettingItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            title = itemView.findViewById(R.id.title);
            switchCompat = itemView.findViewById(R.id.switch_compat);
        }

        void bind(SettingItem.Switch item) {
            title.setText(item.title);
            // Gỡ listener cũ để tránh trigger vòng lặp vô hạn khi bind
            switchCompat.setOnCheckedChangeListener(null);
            switchCompat.setChecked(item.isEnabled);
            // Gán lại listener mới
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Chỉ gọi listener nếu trạng thái thực sự thay đổi bởi người dùng
                if (buttonView.isPressed()) {
                    item.isEnabled = isChecked; // Cập nhật trạng thái trong model
                    listener.onSwitchItemChanged(item.tag, isChecked);
                }
            });
        }
    }

    static class LogoutViewHolder extends RecyclerView.ViewHolder {
        LogoutViewHolder(@NonNull View itemView, OnSettingItemClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(v -> listener.onNavigationItemClick("logout"));
        }
    }
}