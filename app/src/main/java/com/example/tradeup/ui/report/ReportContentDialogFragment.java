// File: src/main/java/com/example/tradeup/ui/report/ReportContentDialogFragment.java
package com.example.tradeup.ui.report;

import android.os.Bundle;
import android.text.Html;
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
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.User;
import com.example.tradeup.data.model.config.ReportReasonConfig;
import com.example.tradeup.databinding.DialogReportContentBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReportContentDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_CONTENT_ID = "content_id";
    private static final String ARG_CONTENT_TYPE = "content_type";
    private static final String ARG_REPORTED_USER_ID = "reported_user_id";

    private DialogReportContentBinding binding;
    private ReportViewModel viewModel;

    // *** XÓA BỎ VIỆC INJECT REPOSITORY VÀO FRAGMENT ***
    // @Inject ItemRepository itemRepository;
    // @Inject UserRepository userRepository;

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
        // *** SỬA Ở ĐÂY: Gọi ViewModel để tải dữ liệu ***
        viewModel.loadReportedContentInfo(contentId, contentType);
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
            if (selectedRadioButton == null || selectedRadioButton.getTag() == null) return;

            String reasonId = (String) selectedRadioButton.getTag();
            String details = binding.editTextAdditionalDetails.getText() != null ?
                    binding.editTextAdditionalDetails.getText().toString().trim() : "";

            viewModel.submitReport(contentId, contentType, reportedUserId, reasonId, details);
        });
    }

    private void observeViewModel() {
        // *** THÊM OBSERVER MỚI NÀY ***
        viewModel.getReportedContentInfo().observe(getViewLifecycleOwner(), content -> {
            if (content instanceof Item) {
                bindItemInfo((Item) content);
            } else if (content instanceof User) {
                bindUserInfo((User) content);
            } else if ("chat".equalsIgnoreCase(contentType)) {
                bindChatInfo();
            } else {
                if (binding != null) {
                    binding.reportedItemContainer.setVisibility(View.GONE);
                }
            }
        });

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

    // *** CÁC HÀM BIND ĐƯỢC TÁCH RA ***
    private void bindItemInfo(Item item) {
        if (item == null || getContext() == null || binding == null) {
            if (binding != null) binding.reportedItemContainer.setVisibility(View.GONE);
            return;
        }
        binding.reportedItemContainer.setVisibility(View.VISIBLE);
        binding.textViewReportingHeader.setText("You are reporting this listing:");
        binding.textViewProductName.setText(item.getTitle());
        String priceText = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(item.getPrice());
        binding.textViewPrice.setText(priceText);
        binding.textViewSellerName.setText("by " + item.getSellerDisplayName());
        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            Glide.with(getContext()).load(item.getImageUrls().get(0)).placeholder(R.drawable.ic_placeholder_image).into(binding.imageViewProduct);
        }
    }

    private void bindUserInfo(User user) {
        if (user == null || getContext() == null || binding == null) {
            if (binding != null) binding.reportedItemContainer.setVisibility(View.GONE);
            return;
        }
        binding.reportedItemContainer.setVisibility(View.VISIBLE);
        binding.textViewReportingHeader.setText("You are reporting this user:");
        binding.textViewProductName.setText(user.getDisplayName());
        binding.textViewPrice.setVisibility(View.GONE);
        binding.textViewSellerName.setVisibility(View.GONE);
        Glide.with(getContext()).load(user.getProfilePictureUrl()).placeholder(R.drawable.ic_person).into(binding.imageViewProduct);
    }

    private void bindChatInfo() {
        if (getContext() == null || binding == null) return;
        binding.reportedItemContainer.setVisibility(View.VISIBLE);
        binding.textViewReportingHeader.setText("You are reporting this conversation:");
        binding.imageViewProduct.setVisibility(View.GONE);
        binding.textViewPrice.setVisibility(View.GONE);
        binding.textViewSellerName.setVisibility(View.GONE);
        String chatInfoText = "Conversation ID: <b>" + contentId + "</b>";
        binding.textViewProductName.setText(Html.fromHtml(chatInfoText, Html.FROM_HTML_MODE_LEGACY));
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
            radioButton.setPadding(0, 24, 0, 24);
            radioGroup.addView(radioButton);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}