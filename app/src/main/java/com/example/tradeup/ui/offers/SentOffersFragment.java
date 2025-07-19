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
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.databinding.FragmentTabbedListBinding;
import com.example.tradeup.ui.adapters.OfferAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Collections;

public class SentOffersFragment extends Fragment implements OfferAdapter.OnOfferActionListener {

    private FragmentTabbedListBinding binding;
    private OffersViewModel viewModel;
    private OfferAdapter adapter;

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
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        adapter = new OfferAdapter(currentUserId, this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getSentOffers().observe(getViewLifecycleOwner(), offersData -> {
            adapter.submitList(offersData != null ? offersData : Collections.emptyList());
            binding.textViewEmpty.setVisibility(offersData == null || offersData.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textViewEmpty.setText("You haven't sent any offers.");
        });

        // Các observer khác giữ nguyên
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && isAdded()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật các hàm listener
    @Override
    public void onAcceptClick(OfferViewData data) {
        viewModel.accept(data.offer);
    }

    @Override
    public void onRejectClick(OfferViewData data) {
        viewModel.reject(data.offer);
    }

    @Override
    public void onCounterClick(OfferViewData data) {
        viewModel.counter(data.offer);
    }

    @Override
    public void onItemClick(OfferViewData data) {
        if(data.relatedItem != null) {
            Toast.makeText(getContext(), "Clicked on item: " + data.relatedItem.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic điều hướng
        } else {
            Toast.makeText(getContext(), "This item is no longer available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}