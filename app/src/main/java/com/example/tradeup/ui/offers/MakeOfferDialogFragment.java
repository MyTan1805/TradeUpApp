package com.example.tradeup.ui.offers;

// package: com.example.tradeup.ui.offers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment; // Cần thiết để set style

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.DialogMakeOfferBinding; // Sử dụng ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MakeOfferDialogFragment extends BottomSheetDialogFragment {

    // Key để truyền dữ liệu Item vào Dialog
    private static final String ARG_ITEM = "arg_item";

    private DialogMakeOfferBinding binding;
    private Item currentItem;
    // TODO: Khởi tạo ViewModel nếu cần xử lý logic phức tạp

    /**
     * Hàm "nhà máy" để tạo Dialog một cách an toàn và truyền đối tượng Item vào.
     * Lưu ý: Item cần phải implements Parcelable để có thể truyền qua Bundle.
     */
    public static MakeOfferDialogFragment newInstance(Item item) {
        MakeOfferDialogFragment fragment = new MakeOfferDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item); // Giả sử Item implements Parcelable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set style để áp dụng góc bo tròn đã định nghĩa trong themes.xml
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);

        if (getArguments() != null) {
            currentItem = getArguments().getParcelable(ARG_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogMakeOfferBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentItem == null) {
            Toast.makeText(getContext(), "Error: Item data is missing.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        setupUI();
        setupClickListeners();
    }

    /**
     * Điền thông tin của sản phẩm lên giao diện.
     */
    private void setupUI() {
        binding.textViewProductName.setText(currentItem.getTitle());
        binding.textViewOriginalPrice.setText(String.format("Original price: $%.2f", currentItem.getPrice()));

        if (currentItem.getImageUrls() != null && !currentItem.getImageUrls().isEmpty()) {
            Glide.with(this)
                    .load(currentItem.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(binding.imageViewProduct);
        }
    }

    /**
     * Gán sự kiện click cho các nút.
     */
    private void setupClickListeners() {
        binding.buttonClose.setOnClickListener(v -> dismiss());
        binding.buttonCancelOffer.setOnClickListener(v -> dismiss());

        binding.buttonSendOffer.setOnClickListener(v -> {
            // TODO: Validate input
            String offerAmount = binding.editTextOfferAmount.getText().toString();
            String message = binding.editTextMessage.getText().toString();

            // TODO: Gọi ViewModel để gửi offer
            Toast.makeText(getContext(), "Sending offer: " + offerAmount, Toast.LENGTH_SHORT).show();

            // Đóng dialog sau khi gửi
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}