// File: src/main/java/com/example/tradeup/ui/messages/ChatDetailFragment.java
package com.example.tradeup.ui.messages;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.databinding.FragmentChatDetailBinding;
import com.example.tradeup.ui.adapters.ChatDetailAdapter;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;

import com.example.tradeup.ui.report.ReportContentDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class ChatDetailFragment extends Fragment {

    private FragmentChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private ChatDetailAdapter adapter;
    private NavController navController;

    private String otherUserName;
    private String chatId;


    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            viewModel.sendImageMessage(imageUri);
                        }
                    }
                });
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

        ChatDetailFragmentArgs args = ChatDetailFragmentArgs.fromBundle(getArguments());
        this.otherUserName = args.getOtherUserName();
        this.chatId = args.getChatId();

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
        binding.buttonAddAttachment.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .galleryOnly()
                    .compress(1024) // Nén ảnh dưới 1MB
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });

        // << THÊM LISTENER CHO MENU TOOLBAR >>
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_view_profile) {
                viewModel.onViewProfileClicked();
                return true;
            } else if (itemId == R.id.action_block_user) {
                viewModel.onBlockUserClicked();
                return true;
            } else if (itemId == R.id.action_report_chat) {
                // Mở dialog report
                if (chatId != null) {
                    ReportContentDialogFragment.newInstance(chatId, "chat", null) // reportedUserId có thể là null cho chat
                            .show(getParentFragmentManager(), "ReportChatDialog");
                }
                return true;
            }
            return false;
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

        viewModel.getNavigateToUserProfileEvent().observe(getViewLifecycleOwner(), event -> {
            String userIdToView = event.getContentIfNotHandled();
            if (userIdToView != null && isAdded()) {
                // Tạo bundle và điều hướng
                Bundle args = new Bundle();
                args.putString("userId", userIdToView);
                navController.navigate(R.id.action_chatDetailFragment_to_publicProfileFragment, args);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}