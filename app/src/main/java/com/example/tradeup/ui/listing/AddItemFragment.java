// package: com.example.tradeup.ui.listing
package com.example.tradeup.ui.listing;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
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
import com.example.tradeup.data.model.ItemLocation;
import com.example.tradeup.data.model.config.AppConfig;
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.databinding.FragmentAddItemBinding;
import com.example.tradeup.ui.adapters.PhotoAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddItemFragment extends Fragment implements PhotoAdapter.OnPhotoActionsListener {

    private static final String TAG = "AddItemFragment";
    private static final String REQUEST_KEY_CATEGORY = "request_category";
    private static final String REQUEST_KEY_CONDITION = "request_condition";

    private FragmentAddItemBinding binding;
    private AddItemViewModel viewModel;
    private PhotoAdapter photoAdapter;
    private ProgressDialog progressDialog;

    // --- Launchers ---
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> placesLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> gpsResolutionLauncher;

    // --- Location-related ---
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // --- Data Holders ---
    private AppConfig appConfig;
    private String selectedCategoryId = null;
    private String selectedConditionId = null;
    private ItemLocation selectedLocation = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddItemViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        initializeLaunchers();
    }

    private void initializeLaunchers() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                if (result.getData().getClipData() != null) {
                    int count = result.getData().getClipData().getItemCount();
                    List<Uri> uris = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        uris.add(result.getData().getClipData().getItemAt(i).getUri());
                    }
                    photoAdapter.addImages(uris);
                } else if (result.getData().getData() != null) {
                    photoAdapter.addImages(List.of(result.getData().getData()));
                }
            } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                Toast.makeText(getContext(), ImagePicker.getError(result.getData()), Toast.LENGTH_SHORT).show();
            }
        });

        placesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Place place = Autocomplete.getPlaceFromIntent(result.getData());
                updateLocationFromPlace(place);
            } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(result.getData());
                Toast.makeText(getContext(), "Lỗi: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                checkGpsAndFetchLocation();
            } else {
                Toast.makeText(getContext(), "Cần cấp quyền vị trí để lấy vị trí mặc định.", Toast.LENGTH_LONG).show();
            }
        });

        gpsResolutionLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                startLocationUpdates();
            } else {
                Toast.makeText(getContext(), "Bạn cần bật GPS để xác định vị trí chính xác.", Toast.LENGTH_SHORT).show();
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
        setupProgressDialog();
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        setupResultListeners();
        loadDefaultLocation(); // Tải vị trí mặc định
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng cập nhật vị trí để tiết kiệm pin khi fragment không còn hiển thị
        stopLocationUpdates();
    }

    // --- Setup Methods ---

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Đang xử lý");
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoAdapter(this);
        binding.recyclerViewImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewImages.setAdapter(photoAdapter);
    }

    private void setupClickListeners() {
        binding.buttonCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.buttonPostListing.setOnClickListener(v -> handlePostListing());

        binding.fieldCategory.setOnClickListener(v -> {
            if (appConfig != null && appConfig.getDisplayCategories() != null) {
                ArrayList<String> categoryNames = appConfig.getDisplayCategories().stream()
                        .map(DisplayCategoryConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                ListSelectionDialogFragment.newInstance("Chọn Danh mục", categoryNames, REQUEST_KEY_CATEGORY)
                        .show(getParentFragmentManager(), "CategoryDialog");
            }
        });

        binding.fieldCondition.setOnClickListener(v -> {
            if (appConfig != null && appConfig.getItemConditions() != null) {
                ArrayList<String> conditionNames = appConfig.getItemConditions().stream()
                        .map(ItemConditionConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                ListSelectionDialogFragment.newInstance("Chọn Tình trạng", conditionNames, REQUEST_KEY_CONDITION)
                        .show(getParentFragmentManager(), "ConditionDialog");
            }
        });

        binding.fieldLocation.setOnClickListener(v -> openPlacesAutocomplete());
    }

    private void setupObservers() {
        viewModel.getAppConfig().observe(getViewLifecycleOwner(), config -> this.appConfig = config);
        viewModel.getAddItemState().observe(getViewLifecycleOwner(), this::handleAddItemState);
    }

    private void setupResultListeners() {
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null) {
                DisplayCategoryConfig selected = appConfig.getDisplayCategories().get(index);
                this.selectedCategoryId = selected.getId();
                binding.fieldCategory.setText(selected.getName());
                binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
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
    }

    // --- Business Logic ---

    private void handleAddItemState(AddItemState state) {
        setLoading(state instanceof AddItemState.Loading);
        if (state instanceof AddItemState.Loading) {
            progressDialog.setMessage(((AddItemState.Loading) state).message);
        } else if (state instanceof AddItemState.Success) {
            Toast.makeText(getContext(), "Đăng tin thành công!", Toast.LENGTH_LONG).show();
            // TODO: Điều hướng đến trang chi tiết của sản phẩm vừa đăng
            NavHostFragment.findNavController(this).navigateUp();
        } else if (state instanceof AddItemState.Error) {
            Toast.makeText(getContext(), "Lỗi: " + ((AddItemState.Error) state).message, Toast.LENGTH_LONG).show();
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            if (!progressDialog.isShowing()) progressDialog.show();
        } else {
            if (progressDialog.isShowing()) progressDialog.dismiss();
        }
        binding.buttonPostListing.setEnabled(!isLoading);
    }

    private void handlePostListing() {
        if (!validateInput()) return;

        Item itemToPost = new Item();
        itemToPost.setTitle(binding.editTextTitle.getText().toString().trim());
        itemToPost.setPrice(Double.parseDouble(binding.editTextPrice.getText().toString().trim()));
        itemToPost.setDescription(binding.editTextDescription.getText().toString().trim());
        itemToPost.setCategory(selectedCategoryId);
        itemToPost.setCondition(selectedConditionId);
        itemToPost.setLocation(selectedLocation);

        viewModel.postItem(itemToPost, photoAdapter.getImageUris());
    }

    private boolean validateInput() {
        if (photoAdapter.getImageUris().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm ít nhất một ảnh.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextTitle.getText())) {
            binding.tilTitle.setError("Tiêu đề không được để trống");
            return false;
        } else {
            binding.tilTitle.setError(null);
        }
        if (TextUtils.isEmpty(binding.editTextPrice.getText())) {
            binding.tilPrice.setError("Giá không được để trống");
            return false;
        } else {
            binding.tilPrice.setError(null);
        }
        if (selectedCategoryId == null) {
            Toast.makeText(getContext(), "Vui lòng chọn danh mục.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedConditionId == null) {
            Toast.makeText(getContext(), "Vui lòng chọn tình trạng.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedLocation == null) {
            Toast.makeText(getContext(), "Vui lòng chọn vị trí.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // --- Location Logic ---

    private void loadDefaultLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGpsAndFetchLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkGpsAndFetchLocation() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(requireActivity(), response -> startLocationUpdates());
        task.addOnFailureListener(requireActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build();
                    gpsResolutionLauncher.launch(intentSenderRequest);
                } catch (Exception sendEx) {
                    // Ignore
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLocations().get(0);
                    getAddressFromLocation(location);
                    stopLocationUpdates();
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                updateLocationFromPlace(addresses.get(0).getAddressLine(0), location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
        }
    }

    private void openPlacesAutocomplete() {
        if (!Places.isInitialized()) return;
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("VN").build(requireContext());
        placesLauncher.launch(intent);
    }

    private void updateLocationFromPlace(Place place) {
        if (place.getAddress() == null || place.getLatLng() == null) return;
        updateLocationFromPlace(place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude);
    }

    private void updateLocationFromPlace(String address, double lat, double lon) {
        this.selectedLocation = new ItemLocation(lat, lon, address, null);
        binding.textCurrentLocation.setText(address);
        binding.textCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
    }


    // --- Photo Adapter Callbacks ---

    @Override
    public void onAddPhotoClick() {
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
    public void onRemovePhotoClick(int position) {
        photoAdapter.removeImage(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}