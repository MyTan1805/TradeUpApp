// File: src/main/java/com/example/tradeup/ui/profile/tabs/ProfileReviewsFragment.java
package com.example.tradeup.ui.profile.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tradeup.databinding.FragmentProfileReviewsBinding;
import com.example.tradeup.ui.adapters.ReviewAdapter;
import com.example.tradeup.ui.profile.ProfileViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileReviewsFragment extends Fragment {

    private FragmentProfileReviewsBinding binding;
    private ProfileViewModel sharedViewModel;
    private ReviewAdapter reviewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileReviewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter();
        binding.recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewReviews.setAdapter(reviewAdapter);
    }

    private void setupObservers() {
        sharedViewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null) {
                reviewAdapter.submitList(reviews);
                binding.textViewEmptyReviews.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}