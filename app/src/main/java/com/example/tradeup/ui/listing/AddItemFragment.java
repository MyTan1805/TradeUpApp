package com.example.tradeup.ui.listing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tradeup.R;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.ItemLocation;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.CategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.data.model.config.SubcategoryConfig;
import com.example.tradeup.databinding.FragmentAddItemBinding;
import com.example.tradeup.ui.adapters.PhotoAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.material.chip.Chip;

@AndroidEntryPoint
public class AddItemFragment extends Fragment implements PhotoAdapter.OnPhotoActionsListener {

    private static final String TAG = "AddItemFragment";
    private static final String REQUEST_KEY_CONDITION = "request_condition";
    private static final String REQUEST_KEY_BEHAVIOR = "request_behavior";
    private static final String REQUEST_KEY_PARENT_CATEGORY = "request_parent_category";
    private static final String REQUEST_KEY_SUB_CATEGORY = "request_sub_category";

    private FragmentAddItemBinding binding;
    private AddItemViewModel viewModel;
    private PhotoAdapter photoAdapter;
    private NavController navController;

    // Các biến trạng thái
    private String selectedParentCategoryId;
    private String selectedSubCategoryId;
    private String selectedConditionId;
    private String selectedItemBehavior;
    private GeoPoint selectedGeoPoint;
    private String selectedAddress;
    private final List<String> selectedTags = new ArrayList<>();
    private AppConfig appConfig;

    // Các biến launcher và location
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<androidx.activity.result.IntentSenderRequest> gpsResolutionLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddItemViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        initializeLaunchers();
    }

    private void initializeLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            photoAdapter.addImages(Collections.singletonList(uri));
                        }
                    } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(getContext(), ImagePicker.getError(result.getData()), Toast.LENGTH_SHORT).show();
                    }
                });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                checkGpsAndFetchLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required for default location.", Toast.LENGTH_LONG).show();
            }
        });

        gpsResolutionLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                startLocationUpdates();
            } else {
                Toast.makeText(getContext(), "GPS is required for accurate location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        setupResultListeners();
        loadDefaultLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoAdapter(this);
        binding.recyclerViewImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewImages.setAdapter(photoAdapter);
    }

    private void setupClickListeners() {
        binding.buttonCancel.setOnClickListener(v -> navController.navigateUp());
        binding.buttonPostListing.setOnClickListener(v -> handlePostListing());
        binding.buttonPreview.setOnClickListener(v -> handlePreview());

        binding.fieldCategory.setOnClickListener(v -> {
            if (appConfig != null && appConfig.getCategories() != null) {
                ArrayList<String> parentCategoryNames = appConfig.getCategories().stream()
                        .map(CategoryConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                ListSelectionDialogFragment.newInstance("Select Category", parentCategoryNames, REQUEST_KEY_PARENT_CATEGORY)
                        .show(getParentFragmentManager(), "ParentCategoryDialog");
            }
        });

        binding.fieldCondition.setOnClickListener(v -> {
            if (appConfig != null && appConfig.getItemConditions() != null) {
                ArrayList<String> conditionNames = appConfig.getItemConditions().stream()
                        .map(ItemConditionConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                ListSelectionDialogFragment.newInstance("Select Condition", conditionNames, REQUEST_KEY_CONDITION)
                        .show(getParentFragmentManager(), "ConditionDialog");
            }
        });

        binding.fieldLocation.setOnClickListener(v -> {
            AddressSearchDialogFragment.newInstance()
                    .show(getParentFragmentManager(), AddressSearchDialogFragment.TAG);
        });

        binding.buttonChangeLocation.setOnClickListener(v -> {
            AddressSearchDialogFragment.newInstance()
                    .show(getParentFragmentManager(), AddressSearchDialogFragment.TAG);
        });

        binding.fieldItemBehavior.setOnClickListener(v -> {
            ArrayList<String> behaviors = new ArrayList<>(AddItemViewModel.ITEM_BEHAVIORS);
            ListSelectionDialogFragment.newInstance("Select Trading Method", behaviors, REQUEST_KEY_BEHAVIOR)
                    .show(getParentFragmentManager(), "BehaviorDialog");
        });
    }

    private void setupObservers() {
        viewModel.getAppConfig().observe(getViewLifecycleOwner(), config -> {
            this.appConfig = config;
            restoreUiState(); // Khôi phục UI sau khi có config
        });

        viewModel.imageUris.observe(getViewLifecycleOwner(), uris -> {
            if (photoAdapter != null) {
                photoAdapter.setImageUris(uris);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.buttonPostListing.setEnabled(!isLoading);
                binding.buttonPreview.setEnabled(!isLoading);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getPostSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            String itemId = event.getContentIfNotHandled();
            if (itemId != null && getContext() != null) {
                Toast.makeText(getContext(), "Listing posted successfully!", Toast.LENGTH_LONG).show();
                Bundle args = new Bundle();
                args.putString("itemId", itemId);
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.addItemFragment, true)
                        .build();
                navController.navigate(R.id.itemDetailFragment, args, navOptions);
            }
        });
    }
    private void updateTextView(TextView textView, String text) {
        if (textView == null || getContext() == null) return;
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
        // Nếu TextView này là một phần của TextInputLayout, bạn có thể xóa lỗi
        // if (textView.getParent().getParent() instanceof TextInputLayout) {
        //     ((TextInputLayout) textView.getParent().getParent()).setError(null);
        // }
    }
    private void updateCategoryTextView(String parentId, String subId) {
        if (appConfig == null || binding == null) return;

        // Nếu không có ID nào, không làm gì cả
        if (parentId == null && subId == null) {
            binding.fieldCategory.setText(R.string.add_item_field_category_select);
            binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            return;
        }

        String displayText = "";

        // Tìm tên danh mục cha
        CategoryConfig parent = null;
        if (parentId != null) {
            parent = appConfig.getCategories().stream()
                    .filter(c -> parentId.equals(c.getId()))
                    .findFirst().orElse(null);
        }

        if (parent != null) {
            displayText = parent.getName();
            // Nếu có cả ID con, tìm tên con và ghép vào
            if (subId != null && !subId.equals(parentId) && parent.getSubcategories() != null) {
                String subName = parent.getSubcategories().stream()
                        .filter(s -> subId.equals(s.getId()))
                        .map(SubcategoryConfig::getName)
                        .findFirst()
                        .orElse("");
                if (!subName.isEmpty()) {
                    displayText += " > " + subName;
                }
            }
        }

        if (!displayText.isEmpty()) {
            updateTextView(binding.fieldCategory, displayText);
        }
    }

    private void restoreUiState() {
        if (appConfig == null || binding == null || getContext() == null) return;

        // Khôi phục Category
        updateCategoryTextView(viewModel.parentCategoryId.getValue(), viewModel.subCategoryId.getValue());

        // Khôi phục Condition
        String conditionId = viewModel.conditionId.getValue();
        if (conditionId != null) {
            appConfig.getItemConditions().stream()
                    .filter(c -> c.getId().equals(conditionId))
                    .findFirst()
                    .ifPresent(c -> updateTextView(binding.fieldCondition, c.getName()));
        }

        // Khôi phục Behavior
        String behavior = viewModel.itemBehavior.getValue();
        if (behavior != null) {
            updateTextView(binding.fieldItemBehavior, behavior);
        }

        // Khôi phục Location
        ItemLocation location = viewModel.location.getValue();
        if (location != null) {
            updateTextView(binding.textCurrentLocation, location.getAddressString());
        } else {
            loadDefaultLocation();
        }

        // Khôi phục Tags
        updateSuggestedTags(viewModel.subCategoryId.getValue() != null ? viewModel.subCategoryId.getValue() : viewModel.parentCategoryId.getValue());
    }

    private void handlePreview() {
        if (!isDataValidForPreview()) {
            Toast.makeText(getContext(), "Please fill in all required fields to preview.", Toast.LENGTH_SHORT).show();
            return;
        }

        Item itemPreview = new Item();
        FirebaseUser currentUser = viewModel.getCurrentUser();
        if (currentUser == null) return;

        List<String> tempImageUrls = new ArrayList<>();
        for (Uri uri : photoAdapter.getImageUris()) {
            tempImageUrls.add(uri.toString());
        }
        itemPreview.setImageUrls(tempImageUrls);

        itemPreview.setTitle(binding.editTextTitle.getText().toString().trim());
        itemPreview.setPrice(Double.parseDouble(binding.editTextPrice.getText().toString().trim()));
        itemPreview.setDescription(binding.editTextDescription.getText().toString().trim());
        itemPreview.setCategory(selectedSubCategoryId != null ? selectedSubCategoryId : selectedParentCategoryId);
        itemPreview.setCondition(selectedConditionId);
        itemPreview.setAddressString(selectedAddress);
        itemPreview.setLocation(selectedGeoPoint);
        itemPreview.setSellerId(currentUser.getUid());
        itemPreview.setSellerDisplayName(currentUser.getDisplayName());
        if (currentUser.getPhotoUrl() != null) {
            itemPreview.setSellerProfilePictureUrl(currentUser.getPhotoUrl().toString());
        }

        Bundle args = new Bundle();
        args.putParcelable("itemPreview", itemPreview);
        navController.navigate(R.id.itemDetailFragment, args);
    }

    private boolean isDataValidForPreview() {
        // Validation ít nghiêm ngặt hơn, không hiển thị lỗi trên field
        return !photoAdapter.getImageUris().isEmpty() &&
                !TextUtils.isEmpty(binding.editTextTitle.getText()) &&
                !TextUtils.isEmpty(binding.editTextPrice.getText()) &&
                (selectedParentCategoryId != null || selectedSubCategoryId != null) &&
                selectedConditionId != null &&
                selectedAddress != null;
    }

    private void setupResultListeners() {
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_PARENT_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null) {
                CategoryConfig selectedParent = appConfig.getCategories().get(index);
                this.selectedParentCategoryId = selectedParent.getId();
                List<SubcategoryConfig> subcategories = selectedParent.getSubcategories();

                if (subcategories != null && !subcategories.isEmpty()) {
                    ArrayList<String> subCategoryNames = subcategories.stream()
                            .map(SubcategoryConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                    ListSelectionDialogFragment.newInstance(selectedParent.getName(), subCategoryNames, REQUEST_KEY_SUB_CATEGORY)
                            .show(getParentFragmentManager(), "SubCategoryDialog");
                } else {
                    this.selectedSubCategoryId = this.selectedParentCategoryId;
                    binding.fieldCategory.setText(selectedParent.getName());
                    binding.fieldCategory.setError(null);
                    binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                    updateSuggestedTags(this.selectedSubCategoryId);
                }
            }
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_SUB_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null && selectedParentCategoryId != null) {
                appConfig.getCategories().stream()
                        .filter(c -> selectedParentCategoryId.equals(c.getId()))
                        .findFirst()
                        .ifPresent(parent -> {
                            SubcategoryConfig selectedSub = parent.getSubcategories().get(index);
                            this.selectedSubCategoryId = selectedSub.getId();
                            String displayText = parent.getName() + " > " + selectedSub.getName();
                            binding.fieldCategory.setText(displayText);
                            binding.fieldCategory.setError(null);
                            binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                            updateSuggestedTags(this.selectedSubCategoryId);
                        });
            }
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_CONDITION, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null) {
                ItemConditionConfig selected = appConfig.getItemConditions().get(index);
                this.selectedConditionId = selected.getId();
                binding.fieldCondition.setText(selected.getName());
                binding.fieldCondition.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
            }
        });

        getParentFragmentManager().setFragmentResultListener(AddressSearchDialogFragment.REQUEST_KEY, this, (requestKey, bundle) -> {
            String address = bundle.getString(AddressSearchDialogFragment.KEY_ADDRESS);
            double latitude = bundle.getDouble(AddressSearchDialogFragment.KEY_LATITUDE);
            double longitude = bundle.getDouble(AddressSearchDialogFragment.KEY_LONGITUDE);
            if (address != null) {
                this.selectedAddress = address;
                this.selectedGeoPoint = new GeoPoint(latitude, longitude);
                binding.textCurrentLocation.setText(address);
                binding.textCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
            }
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_BEHAVIOR, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1) {
                selectedItemBehavior = AddItemViewModel.ITEM_BEHAVIORS.get(index);
                binding.fieldItemBehavior.setText(selectedItemBehavior);
                binding.fieldItemBehavior.setError(null);
                binding.fieldItemBehavior.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
            }
        });
    }

    private void updateSuggestedTags(String categoryId) {
        if (appConfig == null || categoryId == null) return;
        List<String> suggestedTags = appConfig.getSuggestedTags().get(categoryId);
        binding.chipGroupTags.removeAllViews();
        selectedTags.clear();
        if (suggestedTags == null || suggestedTags.isEmpty()) {
            binding.textViewTagsInstruction.setVisibility(View.VISIBLE);
            binding.chipGroupTags.setVisibility(View.GONE);
        } else {
            binding.textViewTagsInstruction.setVisibility(View.GONE);
            binding.chipGroupTags.setVisibility(View.VISIBLE);
            for (String tagName : suggestedTags) {
                binding.chipGroupTags.addView(createTagChip(tagName));
            }
        }
    }

    private Chip createTagChip(String tagName) {
        Chip chip = new Chip(requireContext());
        chip.setText(tagName);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.chip_background_color_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedTags.size() < 5) {
                    selectedTags.add(tagName);
                } else {
                    Toast.makeText(getContext(), "You can select up to 5 tags.", Toast.LENGTH_SHORT).show();
                    chip.setChecked(false);
                }
            } else {
                selectedTags.remove(tagName);
            }
        });
        return chip;
    }

    private void handlePostListing() {
        if (!validateInput()) return;
        Item itemToPost = new Item();
        itemToPost.setTitle(binding.editTextTitle.getText().toString().trim());
        try {
            itemToPost.setPrice(Double.parseDouble(binding.editTextPrice.getText().toString().trim()));
        } catch (NumberFormatException e) {
            binding.tilPrice.setError("Invalid price");
            return;
        }
        itemToPost.setDescription(binding.editTextDescription.getText().toString().trim());
        itemToPost.setCategory(selectedSubCategoryId != null ? selectedSubCategoryId : selectedParentCategoryId);
        itemToPost.setCondition(selectedConditionId);
        itemToPost.setItemBehavior(selectedItemBehavior);
        itemToPost.setTags(new ArrayList<>(selectedTags));
        if (selectedGeoPoint != null && selectedAddress != null) {
            itemToPost.setLocation(selectedGeoPoint);
            itemToPost.setAddressString(selectedAddress);
        }
        itemToPost.setSearchKeywords(generateKeywords(itemToPost.getTitle()));
        viewModel.postItem(itemToPost, photoAdapter.getImageUris());
    }

    private List<String> generateKeywords(String inputText) {
        if (inputText == null || inputText.isEmpty()) return new ArrayList<>();
        String cleanText = inputText.toLowerCase(Locale.getDefault()).replaceAll("[^a-z0-9\\s]", "");
        String[] words = cleanText.split("\\s+");
        return new ArrayList<>(new HashSet<>(Arrays.asList(words)));
    }

    private boolean validateInput() {
        if (photoAdapter.getImageUris().isEmpty()) {
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
        try {
            double price = Double.parseDouble(binding.editTextPrice.getText().toString().trim());
            if (price < 0) {
                binding.tilPrice.setError("Price cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.tilPrice.setError("Invalid price format");
            return false;
        }
        if (selectedSubCategoryId == null && selectedParentCategoryId == null) {
            binding.fieldCategory.setError("Please select a category");
            return false;
        } else {
            binding.fieldCategory.setError(null);
        }
        if (selectedConditionId == null) {
            Toast.makeText(getContext(), "Please select a condition.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedGeoPoint == null || selectedAddress == null) {
            Toast.makeText(getContext(), "Please select a location.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedItemBehavior == null) {
            Toast.makeText(getContext(), "Please select a trading method.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadDefaultLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGpsAndFetchLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkGpsAndFetchLocation() {
        if (getActivity() == null) return;
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(requireActivity(), response -> startLocationUpdates());
        task.addOnFailureListener(requireActivity(), e -> {
            if (e instanceof com.google.android.gms.common.api.ResolvableApiException) {
                try {
                    androidx.activity.result.IntentSenderRequest intentSenderRequest = new androidx.activity.result.IntentSenderRequest.Builder(
                            ((com.google.android.gms.common.api.ResolvableApiException) e).getResolution()
                    ).build();
                    gpsResolutionLauncher.launch(intentSenderRequest);
                } catch (Exception ignored) {}
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLocations().get(0);
                    getAddressFromNominatim(location);
                    stopLocationUpdates();
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void getAddressFromNominatim(Location location) {
        if (getContext() == null) return;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.getLatitude() + "&lon=" + location.getLongitude())
                .header("User-Agent", "TradeUp/1.0 (your.email@example.com)")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) return;
                final String responseBodyString = response.body() != null ? response.body().string() : null;
                activity.runOnUiThread(() -> {
                    if (response.isSuccessful() && responseBodyString != null) {
                        try {
                            JSONObject json = new JSONObject(responseBodyString);
                            String address = json.getString("display_name");
                            selectedGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            selectedAddress = address;
                            if (binding != null) {
                                binding.textCurrentLocation.setText(address);
                                binding.textCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to parse location data.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Nominatim error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) return;
                activity.runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get address.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onAddPhotoClick() {
        if (photoAdapter.getImageUris().size() >= 10) {
            Toast.makeText(getContext(), "Maximum of 10 photos.", Toast.LENGTH_SHORT).show();
            return;
        }
        ImagePicker.with(this)
                .galleryOnly().crop().compress(1024).maxResultSize(1080, 1080)
                .createIntent(intent -> {
                    imagePickerLauncher.launch(intent);
                    return null;
                });
    }

    @Override
    public void onRemovePhotoClick(int position) {
        photoAdapter.removeImage(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}