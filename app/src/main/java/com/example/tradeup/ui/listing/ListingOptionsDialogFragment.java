package com.example.tradeup.ui.listing;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.DialogListingOptionsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ListingOptionsDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ListingOptionsDialog";

    // Không cần các hằng số REQUEST_KEY, KEY_ACTION nữa

    private DialogListingOptionsBinding binding;
    private MyListingsViewModel viewModel;

    public static ListingOptionsDialogFragment newInstance() {
        return new ListingOptionsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ Fragment cha (MyListingsFragment) để chia sẻ trạng thái
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

        Item selectedItem = viewModel.getSelectedItem();
        if (selectedItem == null) {
            Toast.makeText(getContext(), "Please select an item first.", Toast.LENGTH_SHORT).show();
            dismiss(); // An toàn, nếu không có item nào được chọn thì đóng dialog
            return;
        }

        // Cập nhật giao diện dựa trên trạng thái của item
        updateUiBasedOnItemStatus(selectedItem);

        // Gán sự kiện click trực tiếp vào các hàm của ViewModel
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
            // Hiển thị dialog xác nhận ngay tại đây
            showDeleteConfirmationDialog();
            // Đóng bottom sheet trước
            dismiss();
        });

        binding.optionCancel.setOnClickListener(v -> dismiss());
    }

    private void updateUiBasedOnItemStatus(Item item) {
        // Cập nhật text cho nút Pause/Resume
        if ("paused".equalsIgnoreCase(item.getStatus())) {
            binding.optionPauseResume.setText("Resume Listing");
        } else {
            binding.optionPauseResume.setText("Pause Listing");
        }

        // Ẩn các tùy chọn không phù hợp nếu sản phẩm đã bán
        if ("sold".equalsIgnoreCase(item.getStatus())) {
            binding.optionEdit.setVisibility(View.GONE);
            binding.optionMarkSold.setVisibility(View.GONE);
            binding.optionPauseResume.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog() {
        // Dùng requireContext() để đảm bảo context không null
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to permanently delete this listing?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Gọi hàm xóa trong ViewModel
                    viewModel.deleteSelectedItem();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}