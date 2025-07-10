// File: src/main/java/com/example/tradeup/ui/messages/ChatDetailFragment.java
package com.example.tradeup.ui.messages;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.databinding.FragmentChatDetailBinding;
import com.example.tradeup.ui.adapters.ChatDetailAdapter;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatDetailFragment extends Fragment {

    private FragmentChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private ChatDetailAdapter adapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        // Lấy tên người dùng từ arguments và đặt làm title
        String otherUserName = ChatDetailFragmentArgs.fromBundle(getArguments()).getOtherUserName();
        binding.toolbar.setTitle(otherUserName);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Tạm thời chưa có avatarUrl, sẽ cập nhật sau
        adapter = new ChatDetailAdapter(currentUserId, null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Quan trọng: Hiển thị từ dưới lên
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        binding.recyclerViewMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        binding.buttonSendMessage.setOnClickListener(v -> {
            String messageText = binding.editTextMessage.getText().toString();
            if (!messageText.trim().isEmpty()) {
                viewModel.sendMessage(messageText);
                binding.editTextMessage.setText(""); // Xóa text sau khi gửi
            }
        });

        // Vô hiệu hóa nút gửi khi không có text
        binding.editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonSendMessage.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        // Set trạng thái ban đầu
        binding.buttonSendMessage.setEnabled(false);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            adapter.submitList(messages, () -> {
                // Cuộn xuống tin nhắn cuối cùng sau khi list được cập nhật
                if (messages != null && !messages.isEmpty()) {
                    binding.recyclerViewMessages.smoothScrollToPosition(messages.size() - 1);
                }
            });
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái gửi tin nhắn để vô hiệu hóa nút
        viewModel.isSending().observe(getViewLifecycleOwner(), isSending -> {
            binding.buttonSendMessage.setEnabled(!isSending);
        });

        viewModel.getOtherUserAvatarUrl().observe(getViewLifecycleOwner(), avatarUrl -> {
            // Tạo adapter MỚI khi có avatar, hoặc cập nhật adapter cũ
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            adapter = new ChatDetailAdapter(currentUserId, avatarUrl);
            binding.recyclerViewMessages.setAdapter(adapter);
            // Sau khi có adapter mới, cần submit lại list tin nhắn đã có
            adapter.submitList(viewModel.getMessages().getValue());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}