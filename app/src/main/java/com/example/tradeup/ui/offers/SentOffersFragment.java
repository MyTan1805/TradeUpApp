// File: src/main/java/com/example/tradeup/ui/offers/SentOffersFragment.java

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
import com.example.tradeup.databinding.FragmentTabbedListBinding; // Tái sử dụng layout này
import com.example.tradeup.ui.adapters.OfferAdapter;

public class SentOffersFragment extends Fragment {

    private FragmentTabbedListBinding binding;
    private OffersViewModel viewModel;
    private OfferAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ Fragment cha (OffersFragment)
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
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        // *** ĐIỂM KHÁC BIỆT 1: isReceivedOffers = false ***
        adapter = new OfferAdapter(false, new OfferAdapter.OnOfferActionListener() {
            @Override
            public void onAccept(Offer offer) { /* Người gửi không thể Accept */ }

            @Override
            public void onReject(Offer offer) { /* Người gửi không thể Reject, nhưng có thể Cancel. Tạm thời bỏ qua */ }

            @Override
            public void onCounter(Offer offer) { /* Người gửi không thể Counter */ }

            @Override
            public void onItemClick(Offer offer) {
                // TODO: Điều hướng đến chi tiết sản phẩm hoặc cuộc trò chuyện
                Toast.makeText(getContext(), "Clicked on item: " + offer.getItemId(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // *** ĐIỂM KHÁC BIỆT 2: Lắng nghe getSentOffers() ***
        viewModel.getSentOffers().observe(getViewLifecycleOwner(), offers -> {
            adapter.submitList(offers);
            binding.textViewEmpty.setVisibility(offers.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textViewEmpty.setText("You haven't sent any offers.");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}