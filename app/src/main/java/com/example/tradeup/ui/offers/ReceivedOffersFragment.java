// File: src/main/java/com/example/tradeup/ui/offers/ReceivedOffersFragment.java
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
import com.google.firebase.auth.FirebaseAuth;
import java.util.Collections;

public class ReceivedOffersFragment extends Fragment implements OfferAdapter.OnOfferActionListener {

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
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        adapter = new OfferAdapter(currentUserId, this, requireContext());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }
    private void observeViewModel() {
        viewModel.getReceivedOffers().observe(getViewLifecycleOwner(), offers -> {
            adapter.submitList(offers != null ? offers : Collections.emptyList());
            binding.textViewEmpty.setVisibility(offers == null || offers.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textViewEmpty.setText("You have no received offers.");
        });

        viewModel.getOpenCounterOfferDialogEvent().observe(getViewLifecycleOwner(), event -> {
            Offer offerToCounter = event.getContentIfNotHandled();
            if (offerToCounter != null && isAdded()) {
                CounterOfferDialogFragment.newInstance(offerToCounter.getOfferId())
                        .show(getParentFragmentManager(), CounterOfferDialogFragment.TAG);
            }
        });

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

    @Override
    public void onAcceptClick(Offer offer) {
        viewModel.accept(offer);
    }

    @Override
    public void onRejectClick(Offer offer) {
        viewModel.reject(offer);
    }

    @Override
    public void onCounterClick(Offer offer) {
        viewModel.counter(offer);
    }

    @Override
    public void onItemClick(Offer offer) {
        Toast.makeText(getContext(), "Clicked on item: " + offer.getItemId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}