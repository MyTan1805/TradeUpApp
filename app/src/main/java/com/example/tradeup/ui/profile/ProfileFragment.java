// File: src/main/java/com/example/tradeup/ui/profile/ProfileFragment.java
package com.example.tradeup.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.tradeup.R;
import com.example.tradeup.data.model.User;
import com.example.tradeup.databinding.FragmentProfileBinding;
import com.example.tradeup.ui.adapters.ProfileTabsAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileTabsAdapter profileTabsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
        setupObservers();
    }

    private void setupViewPager() {
        profileTabsAdapter = new ProfileTabsAdapter(this);
        binding.viewPagerProfileContent.setAdapter(profileTabsAdapter);

        new TabLayoutMediator(binding.tabLayoutProfile, binding.viewPagerProfileContent,
                (tab, position) -> tab.setText(profileTabsAdapter.getTabTitle(position))
        ).attach();
    }

    private void setupObservers() {
        viewModel.getHeaderState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ProfileHeaderState.Loading) {
                // You can show a loading indicator here if needed
            } else if (state instanceof ProfileHeaderState.Success) {
                bindHeaderData(((ProfileHeaderState.Success) state).user, ((ProfileHeaderState.Success) state).isCurrentUserProfile);
            } else if (state instanceof ProfileHeaderState.Error) {
                Toast.makeText(getContext(), ((ProfileHeaderState.Error) state).message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindHeaderData(@NonNull User user, boolean isCurrentUserProfile) {
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewProfilePicture);

        binding.textViewUserName.setText(user.getDisplayName());
        binding.ratingBarUser.setRating((float) user.getAverageRating());
        binding.textViewRatingValue.setText(String.format(Locale.getDefault(), "%.1f", user.getAverageRating()));
        binding.textViewReviewCount.setText(String.format(Locale.getDefault(), "%d Reviews", user.getReviewCount()));

        // Update stats from denormalized fields on User model
        binding.textStatListings.setText(String.valueOf(user.getTotalListings()));
        binding.textStatSold.setText(String.valueOf(user.getTotalTransactions()));
        // binding.textStatFollowers.setText(...); // Add if you have a followersCount field

        // Show/hide buttons based on whether this is the current user's profile
        int visibility = isCurrentUserProfile ? View.VISIBLE : View.GONE;
        binding.buttonEditProfile.setVisibility(visibility);
        binding.fabChangeProfilePicture.setVisibility(visibility);
        binding.layoutSecondaryActions.setVisibility(visibility);
        binding.buttonSettings.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.INVISIBLE); // Keep layout space
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}