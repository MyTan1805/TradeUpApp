// File: src/main/java/com/example/tradeup/ui/notifications/NotificationsFragment.java
package com.example.tradeup.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Notification;
import com.example.tradeup.databinding.FragmentNotificationsBinding;
import com.example.tradeup.ui.adapters.NotificationAdapter;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        // Có thể thêm listener cho các chip filter hoặc nút "Mark all as read" ở đây
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            adapter.submitList(notifications);
            // TODO: Hiển thị/ẩn empty view
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Hiển thị/ẩn progress bar
        });
        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            Notification notification = event.getContentIfNotHandled();
            if (notification != null) {
                handleNavigation(notification);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String msg = event.getContentIfNotHandled();
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        viewModel.onNotificationClicked(notification);
    }
    private void handleNavigation(Notification notification) {
        if (!isAdded() || notification.getRelatedContentId() == null) {
            // Không thực hiện hành động nếu fragment chưa được gắn hoặc không có ID liên quan
            return;
        }

        switch (notification.getType()) {
            // CÁC TRƯỜNG HỢP CẦN ĐIỀU HƯỚNG ĐẾN CHI TIẾT SẢN PHẨM
            case "new_offer":
                try {
                    // Dùng action toàn cục đã tạo
                    navController.navigate(R.id.action_global_to_offersFragment);
                } catch (Exception e) {
                    Log.e("NavError", "Failed to navigate to OffersFragment", e);
                    Toast.makeText(getContext(), "Could not open your offers.", Toast.LENGTH_SHORT).show();
                }
                break;
            case "offer_rejected":
            case "offer_countered":
            case "listing_update":
                Bundle itemArgs = new Bundle();
                itemArgs.putString("itemId", notification.getRelatedContentId());
                try {
                    navController.navigate(R.id.action_global_to_itemDetailFragment, itemArgs);
                } catch (Exception e) {
                    Log.e("NavError", "Failed to navigate to ItemDetailFragment", e);
                    Toast.makeText(getContext(), "Could not open item details.", Toast.LENGTH_SHORT).show();
                }
                break;

            // TRƯỜNG HỢP OFFER ĐƯỢC CHẤP NHẬN -> ĐẾN LỊCH SỬ GIAO DỊCH
            case "offer_accepted":
                try {
                    // Người mua cần xem lịch sử giao dịch để tiến hành thanh toán
                    navController.navigate(R.id.action_global_to_transactionHistoryFragment);
                } catch (Exception e) {
                    Log.e("NavError", "Failed to navigate to TransactionHistoryFragment", e);
                    Toast.makeText(getContext(), "Could not open transaction history.", Toast.LENGTH_SHORT).show();
                }
                break;

            // TRƯỜNG HỢP TIN NHẮN MỚI -> ĐẾN CHAT (SẼ LÀM SAU)
            case "new_message":
                Bundle chatArgs = new Bundle();
                chatArgs.putString("chatId", notification.getRelatedContentId());
                // try {
                //     navController.navigate(R.id.action_global_to_chatDetailFragment, chatArgs);
                // } catch (Exception e) { ... }
                Toast.makeText(getContext(), "Navigate to chat: " + notification.getRelatedContentId(), Toast.LENGTH_SHORT).show();
                break;

            // CÁC TRƯỜNG HỢP KHÁC
            default:
                Toast.makeText(getContext(), "No specific action for this notification.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}