// File: src/main/java/com/example/tradeup/ui/reviews/SubmitReviewFragment.java
package com.example.tradeup.ui.reviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentSubmitReviewBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SubmitReviewFragment extends Fragment {

    private FragmentSubmitReviewBinding binding;
    private SubmitReviewViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SubmitReviewViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSubmitReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> navigateBack());
        binding.buttonSkip.setOnClickListener(v -> navigateBack());
        binding.buttonSkipThisStep.setOnClickListener(v -> navigateBack());

        binding.buttonSubmitReview.setOnClickListener(v -> {
            int rating = (int) binding.ratingBar.getRating();
            String feedback = binding.editTextReview.getText().toString().trim();
            viewModel.submitReview(rating, feedback);
        });
    }

    private void observeViewModel() {
        viewModel.getRatedUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                bindUserInfo(user);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.buttonSubmitReview.setEnabled(!isLoading);
            binding.buttonSubmitReview.setText(isLoading ? "Submitting..." : "Submit Review");
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
                Toast.makeText(getContext(), "Review submitted successfully, thank you!", Toast.LENGTH_LONG).show();
                navigateBack();
            }
        });
    }

    private void bindUserInfo(User user) {
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .into(binding.imageViewUser);
        binding.textViewUserName.setText(user.getDisplayName());
        // TODO: Hiển thị thêm tên sản phẩm nếu cần, bằng cách truyền itemId qua arguments
    }

    private void navigateBack() {
        if (isAdded()) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}