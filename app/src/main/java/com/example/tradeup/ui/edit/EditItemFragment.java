package com.example.tradeup.ui.edit;

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
import com.example.tradeup.data.model.config.DisplayCategoryConfig;
import com.example.tradeup.data.model.config.ItemConditionConfig;
import com.example.tradeup.databinding.FragmentEditItemBinding;
import com.example.tradeup.ui.adapters.EditImageAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
import com.example.tradeup.ui.listing.AddItemViewModel;
import com.example.tradeup.ui.listing.ImageSource;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationSettingsResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@AndroidEntryPoint
public class EditItemFragment extends Fragment implements EditImageAdapter.OnImageActionsListener {

    private static final String TAG = "EditItemFragment";
    private static final String REQUEST_KEY_CATEGORY = "request_category";
    private static final String REQUEST_KEY_CONDITION = "request_condition";

    private FragmentEditItemBinding binding;
    private EditItemViewModel viewModel;
    private EditImageAdapter imageAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> placesLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<androidx.activity.result.IntentSenderRequest> gpsResolutionLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private AppConfig appConfig;
    private String selectedCategoryId;
    private String selectedConditionId;
    private GeoPoint selectedGeoPoint;
    private String selectedAddress;
    private final List<ImageSource> imageSources = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditItemViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (getContext() != null && !Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), getString(R.string.maps_api_key));
        }

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
                    binding.fieldLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                }
            } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(getContext(), "Lỗi chọn địa điểm. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
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
        binding = FragmentEditItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        loadDefaultLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
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

        binding.fieldCategory.setOnClickListener(v -> {
            if (appConfig != null && appConfig.getCategories() != null) { // <-- SỬA Ở ĐÂY
                ArrayList<String> categoryNames = appConfig.getCategories().stream() // <-- SỬA Ở ĐÂY
                        .map(CategoryConfig::getName).collect(Collectors.toCollection(ArrayList::new)); // <-- SỬA Ở ĐÂY
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

    private boolean validateInputs() {
        if (imageSources.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm ít nhất một ảnh.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextTitle.getText())) {
            binding.editTextTitle.setError("Tiêu đề không được để trống");
            return false;
        } else {
            binding.editTextTitle.setError(null);
        }
        String priceText = binding.editTextPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceText)) {
            binding.editTextPrice.setError("Giá không được để trống");
            return false;
        } else {
            binding.editTextPrice.setError(null);
        }
        try {
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                binding.editTextPrice.setError("Giá không được âm");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.editTextPrice.setError("Giá phải là số hợp lệ");
            return false;
        }
        if (TextUtils.isEmpty(binding.fieldCategory.getText())) {
            binding.fieldCategory.setError("Danh mục không được để trống");
            return false;
        } else {
            binding.fieldCategory.setError(null);
        }
        if (TextUtils.isEmpty(binding.fieldCondition.getText())) {
            binding.fieldCondition.setError("Tình trạng không được để trống");
            return false;
        } else {
            binding.fieldCondition.setError(null);
        }
        if (TextUtils.isEmpty(binding.fieldLocation.getText()) || selectedGeoPoint == null || selectedAddress == null) {
            binding.fieldLocation.setError("Vị trí không được để trống");
            return false;
        } else {
            binding.fieldLocation.setError(null);
        }
        return true;
    }

    private void observeViewModel() {
        viewModel.getItem().observe(getViewLifecycleOwner(), item -> {
            if (item != null && binding != null) {
                binding.editTextTitle.setText(item.getTitle());
                binding.editTextDescription.setText(item.getDescription());
                binding.editTextPrice.setText(String.valueOf(item.getPrice()));
                selectedCategoryId = item.getCategory();
                selectedConditionId = item.getCondition();
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
                Toast.makeText(getContext(), "Đã cập nhật sản phẩm!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

        // Observe AppConfig từ AddItemViewModel để lấy danh mục và tình trạng
        new ViewModelProvider(requireActivity()).get(AddItemViewModel.class).getAppConfig().observe(getViewLifecycleOwner(), config -> {
            this.appConfig = config;
        });
    }

    private void setupResultListeners() {
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null) {
                CategoryConfig selected = appConfig.getCategories().get(index); // <-- SỬA Ở ĐÂY
                this.selectedCategoryId = selected.getId();
                binding.fieldCategory.setText(selected.getName());
                binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                binding.fieldCategory.setError(null);
            }
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_CONDITION, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null) {
                ItemConditionConfig selected = appConfig.getItemConditions().get(index);
                this.selectedConditionId = selected.getId();
                binding.fieldCondition.setText(selected.getName());
                binding.fieldCondition.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                binding.fieldCondition.setError(null);
            }
        });
    }

    private void loadDefaultLocation() {
        Item item = viewModel.getItem().getValue();
        if (item != null && item.getLocation() != null && item.getAddressString() != null) {
            selectedGeoPoint = item.getLocation();
            selectedAddress = item.getAddressString();
            binding.fieldLocation.setText(selectedAddress);
            binding.fieldLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
        } else if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                } catch (Exception ignored) {
                    Toast.makeText(getContext(), "Lỗi khi yêu cầu bật GPS", Toast.LENGTH_SHORT).show();
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
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String address = json.getString("display_name");
                        requireActivity().runOnUiThread(() -> {
                            selectedGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            selectedAddress = address;
                            binding.fieldLocation.setText(address);
                            binding.fieldLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                            binding.fieldLocation.setError(null);
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Không thể lấy địa chỉ từ Nominatim", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi Nominatim: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Không thể lấy địa chỉ", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openPlacesAutocomplete() {
        if (getContext() == null || !Places.isInitialized()) return;
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("VN")
                .build(requireContext());
        placesLauncher.launch(intent);
    }

    @Override
    public void onAddImageClick() {
        if (imageSources.size() >= 5) {
            Toast.makeText(getContext(), "Tối đa 5 hình ảnh", Toast.LENGTH_SHORT).show();
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