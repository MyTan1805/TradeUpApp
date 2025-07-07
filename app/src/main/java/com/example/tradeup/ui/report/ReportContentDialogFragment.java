// File: src/main/java/com/example/tradeup/ui/report/ReportContentDialogFragment.java

package com.example.tradeup.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.ReportReasonConfig;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.databinding.DialogReportContentBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject; // *** THÊM IMPORT NÀY ***
import dagger.hilt.android.AndroidEntryPoint;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.repository.UserRepository;

@AndroidEntryPoint
public class ReportContentDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_CONTENT_ID = "content_id";
    private static final String ARG_CONTENT_TYPE = "content_type";
    private static final String ARG_REPORTED_USER_ID = "reported_user_id";

    private DialogReportContentBinding binding;
    private ReportViewModel viewModel;

    // Inject ItemRepository để lấy thông tin sản phẩm
    @Inject
    ItemRepository itemRepository;
    @Inject
    UserRepository userRepository;

    private String contentId;
    private String contentType;
    private String reportedUserId;
    private List<ReportReasonConfig> reportReasons;

    public static ReportContentDialogFragment newInstance(String contentId, String contentType, String reportedUserId) {
        ReportContentDialogFragment fragment = new ReportContentDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT_ID, contentId);
        args.putString(ARG_CONTENT_TYPE, contentType);
        args.putString(ARG_REPORTED_USER_ID, reportedUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        if (getArguments() != null) {
            contentId = getArguments().getString(ARG_CONTENT_ID);
            contentType = getArguments().getString(ARG_CONTENT_TYPE);
            reportedUserId = getArguments().getString(ARG_REPORTED_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogReportContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        observeViewModel();
        // Tải thông tin của nội dung bị báo cáo (sản phẩm, người dùng,...)
        loadReportedContentInfo();
    }

    private void loadReportedContentInfo() {
        if ("listing".equalsIgnoreCase(contentType) && contentId != null) {
            binding.reportedItemContainer.setVisibility(View.VISIBLE);
            binding.textViewReportingHeader.setText("You are reporting this listing:");

            itemRepository.getItemById(contentId, new Callback<Item>() {
                @Override
                public void onSuccess(Item item) {
                    // Kiểm tra xem fragment/context còn tồn tại không trước khi cập nhật UI
                    if (item != null && getContext() != null && binding != null) {
                        binding.textViewProductName.setText(item.getTitle());
                        String priceText = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(item.getPrice());
                        binding.textViewPrice.setText(priceText);
                        binding.textViewSellerName.setText("by " + item.getSellerDisplayName());

                        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                            Glide.with(getContext())
                                    .load(item.getImageUrls().get(0))
                                    .placeholder(R.drawable.ic_placeholder_image)
                                    .into(binding.imageViewProduct);
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (binding != null) {
                        binding.reportedItemContainer.setVisibility(View.GONE);
                    }
                }
            });
        } else if ("profile".equalsIgnoreCase(contentType) && contentId != null) {
            binding.reportedItemContainer.setVisibility(View.VISIBLE);
            binding.textViewReportingHeader.setText("You are reporting this user:");

            // Dùng userRepository để lấy thông tin người bị report
            userRepository.getUserProfile(contentId, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && getContext() != null && binding != null) {
                        binding.textViewProductName.setText(user.getDisplayName());
                        // Ẩn giá và tên người bán
                        binding.textViewPrice.setVisibility(View.GONE);
                        binding.textViewSellerName.setVisibility(View.GONE);

                        Glide.with(getContext())
                                .load(user.getProfilePictureUrl())
                                .placeholder(R.drawable.ic_person) // Dùng icon person làm placeholder
                                .into(binding.imageViewProduct);
                    }
                }

                @Override
                public void onFailure(@NonNull Exception e) {
                    if (binding != null) {
                        binding.reportedItemContainer.setVisibility(View.GONE);
                    }
                }
            });

        } else {
            if (binding != null) {
                binding.reportedItemContainer.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners() {
        binding.buttonClose.setOnClickListener(v -> dismiss());

        binding.buttonSubmitReport.setOnClickListener(v -> {
            int selectedRadioButtonId = binding.radioGroupReasons.getCheckedRadioButtonId();
            if (selectedRadioButtonId == -1) {
                Toast.makeText(getContext(), "Please select a reason.", Toast.LENGTH_SHORT).show();
                return;
            }

            View fragmentView = getView();
            if (fragmentView == null) return;

            RadioButton selectedRadioButton = fragmentView.findViewById(selectedRadioButtonId);
            if (selectedRadioButton == null) return;

            String reasonId = (String) selectedRadioButton.getTag();
            String details = binding.editTextAdditionalDetails.getText() != null ?
                    binding.editTextAdditionalDetails.getText().toString().trim() : "";

            viewModel.submitReport(contentId, contentType, reportedUserId, reasonId, details);
        });
    }

    private void observeViewModel() {
        viewModel.getAppConfig().observe(getViewLifecycleOwner(), appConfig -> {
            if (appConfig != null && appConfig.getReportReasons() != null) {
                this.reportReasons = appConfig.getReportReasons();
                populateReportReasons(this.reportReasons);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.buttonSubmitReport.setEnabled(!isLoading);
            binding.buttonSubmitReport.setText(isLoading ? "Submitting..." : "Submit Report");
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSubmitSuccess().observe(getViewLifecycleOwner(), event -> {
            Boolean isSuccess = event.getContentIfNotHandled();
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Report submitted successfully. Thank you!", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    private void populateReportReasons(List<ReportReasonConfig> reasons) {
        if (getContext() == null || binding == null) return;

        RadioGroup radioGroup = binding.radioGroupReasons;
        radioGroup.removeAllViews();

        for (ReportReasonConfig reason : reasons) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(reason.getName());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(reason.getId());
            radioButton.setPadding(0, 24, 0, 24); // Tăng padding cho dễ nhấn hơn
            radioGroup.addView(radioButton);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}