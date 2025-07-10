// File: src/main/java/com/example/tradeup/ui/messages/MessagesFragment.java
package com.example.tradeup.ui.messages;

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
import com.example.tradeup.data.model.Chat;
import com.example.tradeup.data.model.ParticipantInfoDetail;
import com.example.tradeup.databinding.FragmentMessagesBinding;
import com.example.tradeup.ui.adapters.ChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import android.os.Bundle;

import java.util.Collections;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MessagesFragment extends Fragment implements ChatAdapter.OnChatClickListener {

    private FragmentMessagesBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();
        observeViewModel();
        // Không cần gọi hàm load, ViewModel sẽ tự làm trong constructor
    }

    private void setupRecyclerView() {
        // ChatAdapter cần context để khởi tạo EntryPoint của Hilt
        chatAdapter = new ChatAdapter(requireContext(), this);
        // Đổi ID RecyclerView cho khớp với layout mới của bạn
        binding.recyclerViewMessages.setAdapter(chatAdapter);
    }

    private void observeViewModel() {
        viewModel.getChatState().observe(getViewLifecycleOwner(), state -> {
            // Ẩn tất cả các view trạng thái trước
            binding.progressBar.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewMessages.setVisibility(View.GONE);

            if (state instanceof ChatState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof ChatState.Success) {
                binding.recyclerViewMessages.setVisibility(View.VISIBLE);
                chatAdapter.submitList(((ChatState.Success) state).chats);
            } else if (state instanceof ChatState.Empty) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                chatAdapter.submitList(Collections.emptyList()); // Đảm bảo list rỗng
            } else if (state instanceof ChatState.Error) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), ((ChatState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onChatClick(Chat chat) {
        if (!isAdded()) return;

        // Lấy thông tin người đối diện từ chat object
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = chat.getParticipants().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);

        String otherUserName = "Chat"; // Tên mặc định
        if (otherUserId != null && chat.getParticipantInfo() != null) {
            ParticipantInfoDetail otherUserInfo = chat.getParticipantInfo().get(otherUserId);
            if (otherUserInfo != null) {
                otherUserName = otherUserInfo.getDisplayName();
            }
        }

        // --- PHẦN ĐƯỢC THAY THẾ ---
        // Thay vì dùng MessagesFragmentDirections, chúng ta tạo Bundle thủ công

        Bundle args = new Bundle();
        args.putString("chatId", chat.getChatId());
        args.putString("otherUserName", otherUserName);

        // Gọi navigate bằng ID của action và Bundle
        try {
            navController.navigate(R.id.action_messagesFragment_to_chatDetailFragment, args);
        } catch (Exception e) {
            // Phòng trường hợp ID action không đúng hoặc lỗi điều hướng
            Log.e("NavigationError", "Failed to navigate to ChatDetailFragment", e);
            Toast.makeText(getContext(), "Could not open chat.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}