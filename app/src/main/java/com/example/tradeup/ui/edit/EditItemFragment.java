package com.example.tradeup.ui.edit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.data.model.config.SubcategoryConfig;
import com.example.tradeup.databinding.FragmentEditItemBinding;
import com.example.tradeup.ui.adapters.EditImageAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
import com.example.tradeup.ui.listing.AddItemViewModel;
import com.example.tradeup.ui.listing.ImageSource;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@AndroidEntryPoint
public class EditItemFragment extends Fragment implements EditImageAdapter.OnImageActionsListener {

    private static final String REQUEST_KEY_CONDITION = "request_condition";

    private FragmentEditItemBinding binding;
    private EditItemViewModel viewModel;
    private EditImageAdapter imageAdapter;
    private AppConfig appConfig;
    private final List<ImageSource> imageSources = new ArrayList<>();

    private String selectedAddress;
    private GeoPoint selectedGeoPoint;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> placesLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditItemViewModel.class);
        initializeLaunchers();
    }

    private void initializeLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            imageSources.add(new ImageSource.NewUri(uri));
                            imageAdapter.submitList(new ArrayList<>(imageSources));
                        }
                    } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(getContext(), ImagePicker.getError(result.getData()), Toast.LENGTH_SHORT).show();
                    }
                });

        placesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Place place = Autocomplete.getPlaceFromIntent(result.getData());
                if (place.getAddress() != null && place.getLatLng() != null) {
                    selectedGeoPoint = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
                    selectedAddress = place.getAddress();
                    binding.fieldLocation.setText(selectedAddress);
                }
            } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(getContext(), "Error selecting location.", Toast.LENGTH_SHORT).show();
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
        setupResultListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        imageAdapter = new EditImageAdapter(this);
        binding.recyclerViewEditImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewEditImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        binding.toolbarEdit.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.buttonSaveChanges.setOnClickListener(v -> {
            if (!validateInputs()) {
                return;
            }
            List<String> existingUrls = imageSources.stream()
                    .filter(s -> s instanceof ImageSource.ExistingUrl)
                    .map(s -> ((ImageSource.ExistingUrl) s).url)
                    .collect(Collectors.toList());
            List<Uri> newUris = imageSources.stream()
                    .filter(s -> s instanceof ImageSource.NewUri)
                    .map(s -> ((ImageSource.NewUri) s).uri)
                    .collect(Collectors.toList());

            viewModel.saveChanges(
                    binding.editTextTitle.getText().toString().trim(),
                    binding.editTextDescription.getText().toString().trim(),
                    binding.editTextPrice.getText().toString().trim(),
                    existingUrls,
                    newUris,
                    selectedGeoPoint,
                    selectedAddress
            );
        });

        binding.fieldCondition.setOnClickListener(v -> {
            if (appConfig != null && appConfig.getItemConditions() != null) {
                ArrayList<String> conditionNames = appConfig.getItemConditions().stream()
                        .map(ItemConditionConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                ListSelectionDialogFragment.newInstance("Select Condition", conditionNames, REQUEST_KEY_CONDITION)
                        .show(getParentFragmentManager(), "ConditionDialog");
            }
        });

        binding.fieldLocation.setOnClickListener(v -> openPlacesAutocomplete());
    }

    private void observeViewModel() {
        viewModel.getItem().observe(getViewLifecycleOwner(), item -> {
            if (item != null && binding != null) {
                binding.editTextTitle.setText(item.getTitle());
                binding.editTextDescription.setText(item.getDescription());
                binding.editTextPrice.setText(String.valueOf(item.getPrice()));

                selectedGeoPoint = item.getLocation();
                selectedAddress = item.getAddressString();
                binding.fieldLocation.setText(selectedAddress != null ? selectedAddress : "");

                imageSources.clear();
                if (item.getImageUrls() != null) {
                    for (String url : item.getImageUrls()) {
                        imageSources.add(new ImageSource.ExistingUrl(url));
                    }
                }
                imageAdapter.submitList(new ArrayList<>(imageSources));

                // Cập nhật tên sau khi có AppConfig
                updateCategoryAndConditionNames(item.getCategory(), item.getCondition());
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBarEdit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.buttonSaveChanges.setEnabled(!isLoading);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), event -> {
            String msg = event.getContentIfNotHandled();
            if (msg != null && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getUpdateSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null && getContext() != null) {
                Toast.makeText(getContext(), "Listing updated successfully!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

        // Lấy AppConfig từ ViewModel dùng chung (AddItemViewModel)
        new ViewModelProvider(requireActivity()).get(AddItemViewModel.class).getAppConfig().observe(getViewLifecycleOwner(), config -> {
            this.appConfig = config;
            if (viewModel.getItem().getValue() != null) {
                Item currentItem = viewModel.getItem().getValue();
                updateCategoryAndConditionNames(currentItem.getCategory(), currentItem.getCondition());
            }
        });
    }

    private void updateCategoryAndConditionNames(String categoryId, String conditionId) {
        if (appConfig == null || binding == null) return;

        // Tìm và hiển thị tên danh mục
        if (categoryId != null) {
            for (CategoryConfig parentCat : appConfig.getCategories()) {
                if (parentCat.getId().equals(categoryId)) {
                    binding.fieldCategory.setText(parentCat.getName());
                    return; // Thoát sau khi tìm thấy
                }
                if (parentCat.getSubcategories() != null) {
                    for (SubcategoryConfig subCat : parentCat.getSubcategories()) {
                        if (subCat.getId().equals(categoryId)) {
                            binding.fieldCategory.setText(parentCat.getName() + " > " + subCat.getName());
                            return; // Thoát sau khi tìm thấy
                        }
                    }
                }
            }
        }

        // Tìm và hiển thị tên tình trạng
        if (conditionId != null) {
            appConfig.getItemConditions().stream()
                    .filter(c -> c.getId().equals(conditionId))
                    .findFirst()
                    .ifPresent(c -> binding.fieldCondition.setText(c.getName()));
        }
    }

    private void setupResultListeners() {
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_CONDITION, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null) {
                ItemConditionConfig selected = appConfig.getItemConditions().get(index);
                viewModel.updateCondition(selected.getId());
                binding.fieldCondition.setText(selected.getName());
                binding.fieldCondition.setError(null);
            }
        });
    }

    private void openPlacesAutocomplete() {
        if (getContext() == null) return;
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), getString(R.string.maps_api_key));
        }
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("VN") // Giới hạn tìm kiếm ở Việt Nam
                .build(requireContext());
        placesLauncher.launch(intent);
    }

    private boolean validateInputs() {
        if (imageSources.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one photo.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextTitle.getText())) {
            binding.tilTitle.setError("Title is required");
            return false;
        } else {
            binding.tilTitle.setError(null);
        }
        if (TextUtils.isEmpty(binding.editTextPrice.getText())) {
            binding.tilPrice.setError("Price is required");
            return false;
        } else {
            binding.tilPrice.setError(null);
        }
        return true;
    }

    @Override
    public void onAddImageClick() {
        if (imageSources.size() >= 10) { // Giới hạn 10 ảnh
            Toast.makeText(getContext(), "You can add a maximum of 10 photos.", Toast.LENGTH_SHORT).show();
            return;
        }
        ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent(intent -> {
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