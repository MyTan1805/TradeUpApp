package com.example.tradeup.ui.edit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeup.data.model.Item;
import com.example.tradeup.databinding.FragmentEditItemBinding;
import com.example.tradeup.ui.adapters.EditImageAdapter;
import com.example.tradeup.ui.listing.ImageSource;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditItemFragment extends Fragment implements EditImageAdapter.OnImageActionsListener {

    private FragmentEditItemBinding binding;
    private EditItemViewModel viewModel;
    private EditImageAdapter imageAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private final List<ImageSource> imageSources = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditItemViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            imageSources.add(new ImageSource.NewUri(uri));
                            imageAdapter.submitList(new ArrayList<>(imageSources));
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        imageAdapter = new EditImageAdapter(this);
        binding.recyclerViewEditImages.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.recyclerViewEditImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        binding.toolbarEdit.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.buttonSaveChanges.setOnClickListener(v -> {
            // Tách các ảnh cũ và mới ra
            List<String> existingUrls = imageSources.stream()
                    .filter(s -> s instanceof ImageSource.ExistingUrl)
                    .map(s -> ((ImageSource.ExistingUrl) s).url)
                    .collect(Collectors.toList());
            List<Uri> newUris = imageSources.stream()
                    .filter(s -> s instanceof ImageSource.NewUri)
                    .map(s -> ((ImageSource.NewUri) s).uri)
                    .collect(Collectors.toList());

            viewModel.saveChanges(
                    binding.editTextTitle.getText().toString(),
                    binding.editTextDescription.getText().toString(),
                    binding.editTextPrice.getText().toString(),
                    existingUrls,
                    newUris
            );
        });
    }

    private void observeViewModel() {
        viewModel.getItem().observe(getViewLifecycleOwner(), item -> {
            if (item != null) {
                binding.editTextTitle.setText(item.getTitle());
                binding.editTextDescription.setText(item.getDescription());
                binding.editTextPrice.setText(String.valueOf(item.getPrice()));
                binding.fieldCategory.setText(item.getCategory());
                binding.fieldCondition.setText(item.getCondition());
                if(item.getLocation() != null) binding.fieldLocation.setText(item.getLocation().getAddressString());

                imageSources.clear();
                for (String url : item.getImageUrls()) {
                    imageSources.add(new ImageSource.ExistingUrl(url));
                }
                imageAdapter.submitList(new ArrayList<>(imageSources));
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.buttonSaveChanges.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), event -> {
            String msg = event.getContentIfNotHandled();
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        });

        viewModel.getUpdateSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                Toast.makeText(getContext(), "Item updated!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    @Override
    public void onAddImageClick() {
        ImagePicker.with(this).createIntent(intent -> {
            imagePickerLauncher.launch(intent);
            return null;
        });
    }

    @Override
    public void onRemoveImageClick(ImageSource imageSource) {
        imageSources.remove(imageSource);
        imageAdapter.submitList(new ArrayList<>(imageSources));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}