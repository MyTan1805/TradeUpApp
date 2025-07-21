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
import com.example.tradeup.ui.components.EmojiPickerDialog;
import com.example.tradeup.ui.report.ReportContentDialogFragment;

import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiEditText;
import androidx.activity.OnBackPressedCallback;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatDetailFragment extends Fragment {

    private FragmentChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private ChatDetailAdapter adapter;
    private NavController navController;

    private EmojiPopup emojiPopup;

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

        if (getArguments() != null) {
            ChatDetailFragmentArgs args = ChatDetailFragmentArgs.fromBundle(getArguments());
            this.otherUserName = args.getOtherUserName();
            this.chatId = args.getChatId();
        }

        binding.toolbar.setTitle(otherUserName);

        setupRecyclerView();
        setupEmojiPopup(binding.rootViewChatDetail, binding.editTextMessage);
        setupListeners();
        observeViewModel();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (emojiPopup != null && emojiPopup.isShowing()) {
                    emojiPopup.dismiss(); // Nếu emoji đang hiện, chỉ cần tắt nó đi
                } else {
                    // Nếu không, thực hiện hành vi back mặc định
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void setupEmojiPopup(View rootView, EmojiEditText editText) {
        // Khởi tạo trực tiếp, không qua Builder
        emojiPopup = new EmojiPopup(rootView, editText);
    }
    private void setupRecyclerView() {
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        adapter = new ChatDetailAdapter(currentUserId, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        binding.recyclerViewMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        binding.buttonSendMessage.setOnClickListener(v -> {
            String messageText = binding.editTextMessage.getText().toString();
            if (!messageText.trim().isEmpty()) {
                viewModel.sendMessage(messageText);
                binding.editTextMessage.setText("");
            }
        });

        // Setup Emoji Button
        binding.buttonEmoji.setOnClickListener(v -> {
            emojiPopup.toggle(); // Chỉ cần gọi hàm toggle
        });

        binding.editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonSendMessage.setEnabled(s.toString().trim().length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.buttonAddAttachment.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .galleryOnly()
                    .compress(1024)
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_view_profile) {
                viewModel.onViewProfileClicked();
                return true;
            } else if (itemId == R.id.action_block_user) {
                viewModel.onBlockUserClicked();
                return true;
            } else if (itemId == R.id.action_report_chat) {
                if (chatId != null) {
                    ReportContentDialogFragment.newInstance(chatId, "chat", null)
                            .show(getParentFragmentManager(), "ReportChatDialog");
                }
                return true;
            }
            return false;
        });
        binding.buttonSendMessage.setEnabled(false);
    }

    private void showEmojiPicker() {
        EmojiPickerDialog emojiDialog = EmojiPickerDialog.newInstance();
        emojiDialog.setOnEmojiSelectedListener(emoji -> {
            // Chèn emoji vào vị trí con trở hiện tại của EditText
            int start = Math.max(binding.editTextMessage.getSelectionStart(), 0);
            int end = Math.max(binding.editTextMessage.getSelectionEnd(), 0);

            String currentText = binding.editTextMessage.getText().toString();
            String newText = currentText.substring(0, start) + emoji + currentText.substring(end);

            binding.editTextMessage.setText(newText);
            // Đặt con trỏ sau emoji vừa chèn
            binding.editTextMessage.setSelection(start + emoji.length());
        });

        emojiDialog.show(getParentFragmentManager(), "EmojiPickerDialog");
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
            // Khi có avatar url, cập nhật nó vào adapter đã có
            if (adapter != null && avatarUrl != null) {
                adapter.setOtherUserAvatarUrl(avatarUrl);
            }
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
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }
        super.onDestroyView();
        binding = null;
    }
}