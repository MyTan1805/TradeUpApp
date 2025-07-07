package com.example.tradeup.ui.payment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentStripePaymentBinding;

import com.example.tradeup.ui.profile.TransactionHistoryViewModel;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import dagger.hilt.android.AndroidEntryPoint;


public class StripePaymentFragment extends Fragment {

    private TransactionHistoryViewModel viewModel;
    private Transaction transaction;
    private PaymentSheet paymentSheet;

    private FragmentStripePaymentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ Activity để chia sẻ trạng thái
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionHistoryViewModel.class);

        if (getArguments() != null) {
            transaction = StripePaymentFragmentArgs.fromBundle(getArguments()).getTransaction();
        }

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // << SỬA LẠI CÁCH INFLATE >>
        binding = FragmentStripePaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeViewModel();

        if (transaction != null) {
            // Bắt đầu luồng thanh toán
            viewModel.startOnlinePaymentFlow(transaction);
        } else {
            Toast.makeText(getContext(), "Transaction data is missing.", Toast.LENGTH_SHORT).show();
            navigateBack();
        }
    }

    private void observeViewModel() {
        viewModel.getPaymentSheetParams().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                java.util.Map<String, String> params = event.getContentIfNotHandled();
                if (params != null) {
                    // Lấy các key mới từ server
                    String publishableKey = params.get("publishableKey");
                    String paymentIntentClientSecret = params.get("clientSecret");
                    String ephemeralKeySecret = params.get("ephemeralKey");
                    String customerId = params.get("customer");

                    // << SỬA LẠI ĐIỀU KIỆN KIỂM TRA >>
                    if (publishableKey != null && paymentIntentClientSecret != null && ephemeralKeySecret != null && customerId != null) {
                        PaymentConfiguration.init(requireContext(), publishableKey);

                        PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(customerId, ephemeralKeySecret);

                        // Hiển thị giao diện thanh toán
                        paymentSheet.presentWithPaymentIntent(
                                paymentIntentClientSecret,
                                new PaymentSheet.Configuration("TradeUp Inc.", customerConfig)
                        );
                    } else {
                        // Lỗi vẫn ở đây nếu có key nào đó null
                        Toast.makeText(getContext(), "Failed to get all required keys from server.", Toast.LENGTH_LONG).show();
                        Log.e("StripePayment", "Missing keys: " + params.toString());
                        navigateBack();
                    }
                }
            }
        });
    }
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Thanh toán thành công
            viewModel.completeOnlinePayment(transaction.getTransactionId());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            // Người dùng đã hủy
            viewModel.failOnlinePayment(transaction.getTransactionId());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            // Thanh toán thất bại
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Toast.makeText(getContext(), "Payment failed: " + failedResult.getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            viewModel.failOnlinePayment(transaction.getTransactionId());
        }
        // Sau khi có kết quả, quay lại màn hình trước
        navigateBack();
    }

    private void navigateBack() {
        if (isAdded()) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }
}