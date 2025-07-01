// File: src/main/java/com/example/tradeup/ui/listing/ListingOptionsDialogFragment.java

package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.DialogListingOptionsBinding;
import com.example.tradeup.ui.profile.ProfileViewModel; // *** SỬA IMPORT NÀY ***
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ListingOptionsDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ListingOptionsDialog";
    public static final String REQUEST_KEY = "listing_options_request";
    public static final String KEY_ACTION = "selected_action";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_MARK_SOLD = "mark_sold";
    public static final String ACTION_PAUSE_RESUME = "pause_resume";
    public static final String ACTION_DELETE = "delete";

    private DialogListingOptionsBinding binding;
    private ProfileViewModel viewModel; // *** SỬA KIỂU DỮ LIỆU CỦA VIEWMODEL ***

    public static ListingOptionsDialogFragment newInstance() {
        return new ListingOptionsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ProfileViewModel từ Fragment cha (MyListingsFragment)
        // Dòng này hoạt động vì MyListingsFragment cũng lấy ViewModel theo scope của chính nó.
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class); // *** SỬA LỚP VIEWMODEL Ở ĐÂY ***
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

        // Lấy item đang được chọn từ ProfileViewModel
        Item selectedItem = viewModel.getSelectedItem().getValue();
        if (selectedItem == null) {
            // Trường hợp hiếm gặp, nhưng nên xử lý
            dismiss();
            return;
        }

        // Cập nhật giao diện dựa trên trạng thái của sản phẩm
        updateUiBasedOnItemStatus(selectedItem);

        // Gán sự kiện click cho từng lựa chọn
        binding.optionEdit.setOnClickListener(v -> sendResultAndDismiss(ACTION_EDIT));
        binding.optionMarkSold.setOnClickListener(v -> sendResultAndDismiss(ACTION_MARK_SOLD));
        binding.optionPauseResume.setOnClickListener(v -> sendResultAndDismiss(ACTION_PAUSE_RESUME));
        binding.optionDelete.setOnClickListener(v -> sendResultAndDismiss(ACTION_DELETE));
        binding.optionCancel.setOnClickListener(v -> dismiss());
    }

    // Tách logic cập nhật UI ra một hàm riêng cho rõ ràng
    private void updateUiBasedOnItemStatus(Item item) {
        // Mặc định hiển thị tất cả các tùy chọn có thể có
        binding.optionEdit.setVisibility(View.VISIBLE);
        binding.optionMarkSold.setVisibility(View.VISIBLE);
        binding.optionPauseResume.setVisibility(View.VISIBLE);
        binding.optionDelete.setVisibility(View.VISIBLE);

        String status = item.getStatus();

        if ("paused".equalsIgnoreCase(status)) {
            binding.optionPauseResume.setText("Resume Listing");
            binding.optionPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0);
        } else {
            binding.optionPauseResume.setText("Pause Listing");
            binding.optionPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
        }

        // Nếu sản phẩm đã bán, chỉ cho phép xóa (hoặc xem, tùy logic sau này)
        if ("sold".equalsIgnoreCase(status)) {
            binding.optionEdit.setVisibility(View.GONE);
            binding.optionMarkSold.setVisibility(View.GONE);
            binding.optionPauseResume.setVisibility(View.GONE);
        }
    }

    private void sendResultAndDismiss(String action) {
        Bundle result = new Bundle();
        result.putString(KEY_ACTION, action);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}