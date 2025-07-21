package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.DialogListingOptionsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ListingOptionsDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ListingOptionsDialog";

    private DialogListingOptionsBinding binding;
    private MyListingsViewModel viewModel;

    public static ListingOptionsDialogFragment newInstance() {
        return new ListingOptionsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(MyListingsViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogListingOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ==========================================================
        // === SỬA LỖI Ở ĐÂY: LẮNG NGHE LIVEDATA THAY VÌ GỌI TRỰC TIẾP ===
        // ==========================================================
        viewModel.getSelectedItemLiveData().observe(getViewLifecycleOwner(), selectedItem -> {
            // Chỉ thực hiện khi selectedItem không null
            if (selectedItem != null) {
                setupUIAndListeners(selectedItem);
            } else {
                // Nếu vì lý do nào đó item là null, đóng dialog và báo lỗi
                Toast.makeText(getContext(), "Error: No item data found.", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    // << TÁCH LOGIC RA HÀM RIÊNG ĐỂ GỌN GÀNG HƠN >>
    private void setupUIAndListeners(Item selectedItem) {
        if (binding == null) return; // Kiểm tra an toàn

        updateUiBasedOnItemStatus(selectedItem);

        binding.optionViewAnalytics.setOnClickListener(v -> {
            ListingAnalyticsDialogFragment.newInstance()
                    .show(getParentFragmentManager(), ListingAnalyticsDialogFragment.TAG);
            dismiss();
        });

        binding.optionEdit.setOnClickListener(v -> {
            viewModel.onEditOptionClicked();
            dismiss();
        });

        binding.optionMarkSold.setOnClickListener(v -> {
            viewModel.updateSelectedItemStatus("sold");
            dismiss();
        });

        binding.optionPauseResume.setOnClickListener(v -> {
            String currentStatus = selectedItem.getStatus();
            String newStatus = "paused".equalsIgnoreCase(currentStatus) ? "available" : "paused";
            viewModel.updateSelectedItemStatus(newStatus);
            dismiss();
        });

        binding.optionDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
            dismiss();
        });

        binding.optionCancel.setOnClickListener(v -> dismiss());
    }


    // Hàm này giữ nguyên
    private void updateUiBasedOnItemStatus(Item item) {
        // ... (logic cũ của bạn ở đây là đúng)
        if ("paused".equalsIgnoreCase(item.getStatus())) {
            binding.optionPauseResume.setText(getString(R.string.my_listings_option_resume));
        } else {
            binding.optionPauseResume.setText(getString(R.string.my_listings_option_pause));
        }

        boolean isSold = "sold".equalsIgnoreCase(item.getStatus());

        // Tùy chọn Edit và Mark Sold chỉ hiển thị khi item đang active
        binding.optionEdit.setVisibility("available".equalsIgnoreCase(item.getStatus()) ? View.VISIBLE : View.GONE);
        binding.optionMarkSold.setVisibility("available".equalsIgnoreCase(item.getStatus()) ? View.VISIBLE : View.GONE);

        // Tùy chọn Pause/Resume chỉ hiển thị khi item chưa bán
        binding.optionPauseResume.setVisibility(isSold ? View.GONE : View.GONE);
    }

    // Hàm này giữ nguyên
    private void showDeleteConfirmationDialog() {
        if(getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to permanently delete this listing?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteSelectedItem())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}