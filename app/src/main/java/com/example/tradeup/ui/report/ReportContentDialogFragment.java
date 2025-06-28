package com.example.tradeup.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tradeup.R;
import com.example.tradeup.databinding.DialogReportContentBinding; // Sử dụng ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReportContentDialogFragment extends BottomSheetDialogFragment {

    // Key để truyền dữ liệu vào Dialog
    private static final String ARG_CONTENT_ID = "content_id";
    private static final String ARG_CONTENT_TYPE = "content_type";

    private DialogReportContentBinding binding;
    private String contentId;
    private String contentType;

    // Hàm "nhà máy" để tạo Dialog một cách an toàn
    public static ReportContentDialogFragment newInstance(String contentId, String contentType) {
        ReportContentDialogFragment fragment = new ReportContentDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT_ID, contentId);
        args.putString(ARG_CONTENT_TYPE, contentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contentId = getArguments().getString(ARG_CONTENT_ID);
            contentType = getArguments().getString(ARG_CONTENT_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dùng ViewBinding để inflate layout
        binding = DialogReportContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý sự kiện click
        binding.buttonCancel.setOnClickListener(v -> dismiss()); // Đóng dialog
        binding.toolbar.setNavigationOnClickListener(v -> dismiss()); // Nếu có nút back trên toolbar

        binding.buttonSubmitReport.setOnClickListener(v -> {
            // TODO: Lấy lý do được chọn từ RadioGroup và nội dung chi tiết
            // String selectedReason = ...;
            // String details = binding.editTextAdditionalDetails.getText().toString();
            // ...
            // Gọi ViewModel để gửi report
            // ...

            // Đóng dialog sau khi gửi
            dismiss();
        });

        // TODO: Hiển thị thông tin sản phẩm/người dùng bị báo cáo
        // binding.textViewReportingHeader.setText("Reporting: " + ...);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}