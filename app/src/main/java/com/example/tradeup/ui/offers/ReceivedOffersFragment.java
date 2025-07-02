package com.example.tradeup.ui.offers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.databinding.FragmentTabbedListBinding;
import com.example.tradeup.ui.adapters.OfferAdapter;
import java.util.Collections; // Import Collections để xử lý list null

public class ReceivedOffersFragment extends Fragment {

    private FragmentTabbedListBinding binding;
    private OffersViewModel viewModel;
    private OfferAdapter adapter;

    // << FIX: KHAI BÁO BIẾN offerActionListener Ở ĐÂY >>
    private OfferAdapter.OnOfferActionListener offerActionListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(OffersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTabbedListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Khởi tạo listener trước khi setup RecyclerView
        createOfferActionListener();
        setupRecyclerView();
        observeViewModel();
    }

    private void createOfferActionListener() {
        this.offerActionListener = new OfferAdapter.OnOfferActionListener() {
            @Override
            public void onAccept(Offer offer) {
                viewModel.acceptOffer(offer);
            }
            @Override
            public void onReject(Offer offer) {
                viewModel.rejectOffer(offer);
            }
            @Override
            public void onCounter(Offer offer) {
                viewModel.counterOffer(offer);
            }
            @Override
            public void onItemClick(Offer offer) {
                // TODO: Điều hướng đến chi tiết sản phẩm hoặc cuộc trò chuyện
                Toast.makeText(getContext(), "Clicked on item: " + offer.getItemId(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupRecyclerView() {
        // Bây giờ code sẽ không báo lỗi nữa
        adapter = new OfferAdapter(true, new OfferAdapter.OnOfferActionListener() {
            @Override
            public void onAccept(Offer offer) {
                viewModel.acceptOffer(offer);
            }

            @Override
            public void onReject(Offer offer) {
                viewModel.rejectOffer(offer);
            }

            @Override
            public void onCounter(Offer offer) {
                // <<< LOGIC MỚI: Gọi hàm trong ViewModel để kích hoạt event >>>
                viewModel.counterOffer(offer);
            }

            @Override
            public void onItemClick(Offer offer) {
                Toast.makeText(getContext(), "Clicked on item: " + offer.getItemId(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getReceivedOffers().observe(getViewLifecycleOwner(), offers -> {
            // << CẢI TIẾN: Xử lý trường hợp list là null để tránh crash >>
            if (offers == null) {
                adapter.submitList(Collections.emptyList());
                binding.textViewEmpty.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(offers);
                binding.textViewEmpty.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
            }
            binding.textViewEmpty.setText("You have no received offers.");
        });

        viewModel.getOpenCounterOfferDialogEvent().observe(getViewLifecycleOwner(), event -> {
            Offer offerToCounter = event.getContentIfNotHandled();
            if (offerToCounter != null && isAdded()) {
                CounterOfferDialogFragment.newInstance(offerToCounter.getOfferId())
                        .show(getParentFragmentManager(), CounterOfferDialogFragment.TAG);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}