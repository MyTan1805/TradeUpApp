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

public class ProfileReviewsFragment extends Fragment {

    private FragmentProfileReviewsBinding binding;
    private ProfileViewModel viewModel;
    private ReviewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProfileViewModel.class);
        // Khởi tạo adapter trong onCreate
        adapter = new ReviewAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileReviewsBinding.inflate(inflater, container, false);

        // Gán LayoutManager và Adapter ngay lập tức
        binding.recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewReviews.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null) {
                adapter.submitList(reviews);
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