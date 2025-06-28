package com.example.tradeup.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
        if (item instanceof SettingItem.Info) return TYPE_INFO; // Tạm thời dùng layout navigation
        if (item instanceof SettingItem.Logout) return TYPE_LOGOUT;
        return -1;
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
            case TYPE_NAVIGATION:
            case TYPE_INFO: // Tạm thời Info và Navigation dùng chung layout
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
                // Xử lý cho Info, tương tự Navigation nhưng có thể ẩn mũi tên và hiện text value
                ((NavigationViewHolder) holder).bindInfo((SettingItem.Info) item);
                break;
            case TYPE_LOGOUT:
                // Không cần bind gì cả
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
        ImageView arrow;
        OnSettingItemClickListener listener;
        Context context;

        NavigationViewHolder(@NonNull View itemView, OnSettingItemClickListener listener) {
            super(itemView);
            this.context = itemView.getContext();
            this.listener = listener;
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            arrow = itemView.findViewById(R.id.arrow);
        }

        void bind(SettingItem.Navigation item) {
            icon.setImageResource(item.iconResId);
            title.setText(item.title);
            arrow.setVisibility(View.VISIBLE);

            if (item.textColor != 0) {
                title.setTextColor(ContextCompat.getColor(context, item.textColor));
            }
            if (item.iconTint != 0) {
                icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.iconTint)));
            }

            itemView.setOnClickListener(v -> listener.onNavigationItemClick(item.tag));
        }

        void bindInfo(SettingItem.Info item) {
            icon.setImageResource(item.iconResId);
            title.setText(item.title);
            arrow.setVisibility(View.GONE); // Ẩn mũi tên

            // Tạo một TextView mới để hiển thị giá trị, hoặc thêm TextView vào layout
            // Tạm thời bỏ qua để đơn giản
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
            switchCompat.setOnCheckedChangeListener(null); // Gỡ listener cũ
            switchCompat.setChecked(item.isEnabled);
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isEnabled = isChecked; // Cập nhật trạng thái trong model
                listener.onSwitchItemChanged(item.tag, isChecked);
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