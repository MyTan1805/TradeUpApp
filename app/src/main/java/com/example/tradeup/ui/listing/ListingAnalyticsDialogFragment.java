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
import com.example.tradeup.databinding.DialogListingAnalyticsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.text.NumberFormat;
import java.util.Locale;

public class ListingAnalyticsDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ListingAnalyticsDialog";

    private DialogListingAnalyticsBinding binding;
    private MyListingsViewModel viewModel;

    public static ListingAnalyticsDialogFragment newInstance() {
        return new ListingAnalyticsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ fragment cha (MyListingsFragment)
        viewModel = new ViewModelProvider(requireParentFragment()).get(MyListingsViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogListingAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getSelectedItemLiveData().observe(getViewLifecycleOwner(), this::bindItemData);
    }

    private void bindItemData(Item item) {
        if (item == null || binding == null) {
            dismiss(); // Tự đóng nếu không có dữ liệu
            return;
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

        binding.textViewAnalyticsItemTitle.setText(item.getTitle());
        binding.textViewAnalyticsViews.setText(numberFormat.format(item.getViewsCount()));
        binding.textViewAnalyticsOffers.setText(numberFormat.format(item.getOffersCount()));
        binding.textViewAnalyticsChats.setText(numberFormat.format(item.getChatsCount()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}