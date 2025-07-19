// File: src/main/java/com/example/tradeup/ui/admin/AdminUserAdapter.java
package com.example.tradeup.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.ItemAdminUserBinding;
import java.util.Objects;

public class AdminUserAdapter extends ListAdapter<User, AdminUserAdapter.UserViewHolder> {

    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClicked(User user);
    }

    public AdminUserAdapter(@NonNull OnUserClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminUserBinding binding;
        private final OnUserClickListener listener;
        private final Context context;

        UserViewHolder(ItemAdminUserBinding binding, OnUserClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.context = itemView.getContext();
        }

        void bind(final User user) {
            binding.textViewDisplayName.setText(user.getDisplayName());
            binding.textViewEmail.setText(user.getEmail());

            Glide.with(context)
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.imageViewAvatar);

            // Cập nhật trạng thái
            if (user.isDeactivated()) {
                binding.chipStatus.setText("Suspended");
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_error);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                binding.chipStatus.setText("Active");
                binding.chipStatus.setChipBackgroundColorResource(R.color.status_success);
                binding.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
            }

            itemView.setOnClickListener(v -> listener.onUserClicked(user));
        }
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUid().equals(newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getDisplayName().equals(newItem.getDisplayName())
                    && oldItem.isDeactivated() == newItem.isDeactivated();
        }
    };
}