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
import com.example.tradeup.data.model.config.SubcategoryConfig;
import com.example.tradeup.databinding.FragmentAddItemBinding;
import com.example.tradeup.ui.adapters.PhotoAdapter;
import com.example.tradeup.ui.dialogs.ListSelectionDialogFragment;
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
//    private static final String REQUEST_KEY_CATEGORY = "request_category";
    private static final String REQUEST_KEY_CONDITION = "request_condition";
    private static final String REQUEST_KEY_BEHAVIOR = "request_behavior";
    private static final String REQUEST_KEY_PARENT_CATEGORY = "request_parent_category";
    private static final String REQUEST_KEY_SUB_CATEGORY = "request_sub_category";
//    private final List<String> currentTags = new ArrayList<>();

    private String selectedParentCategoryId; // ID của danh mục cha đã chọn
    private String selectedSubCategoryId;
    private FragmentAddItemBinding binding;
    private AddItemViewModel viewModel;
    private PhotoAdapter photoAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> placesLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<androidx.activity.result.IntentSenderRequest> gpsResolutionLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private AppConfig appConfig;
//    private String selectedCategoryId;
    private String selectedConditionId;
    private GeoPoint selectedGeoPoint;
    private String selectedAddress;
    private final List<String> selectedTags = new ArrayList<>();

    private String selectedItemBehavior;

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
        binding.buttonCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.buttonPostListing.setOnClickListener(v -> handlePostListing());

        binding.fieldCategory.setOnClickListener(v -> {
            // Mở dialog chọn danh mục cha trước
            if (appConfig != null && appConfig.getCategories() != null) {
                ArrayList<String> parentCategoryNames = appConfig.getCategories().stream()
                        .map(CategoryConfig::getName).collect(Collectors.toCollection(ArrayList::new));
                ListSelectionDialogFragment.newInstance("Chọn Danh mục", parentCategoryNames, REQUEST_KEY_PARENT_CATEGORY)
                        .show(getParentFragmentManager(), "ParentCategoryDialog");
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
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.buttonPostListing.setEnabled(!isLoading);
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
                Toast.makeText(getContext(), "Đăng tin thành công!", Toast.LENGTH_LONG).show();
                Bundle args = new Bundle();
                args.putString("itemId", itemId);
                // Thay action_to_detail bằng điều hướng trực tiếp
                NavHostFragment.findNavController(this).navigate(R.id.itemDetailFragment, args);
            }
        });
    }

    private void setupResultListeners() {
        // 1. Lắng nghe kết quả từ Dialog chọn Danh mục Cha
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
                    this.selectedSubCategoryId = this.selectedParentCategoryId; // Không có con thì lấy cha
                    binding.fieldCategory.setText(selectedParent.getName());
                    binding.fieldCategory.setError(null); // Xóa lỗi nếu có
                    binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
                    updateSuggestedTags(this.selectedSubCategoryId);
                }
            }
        });

        // 2. Lắng nghe kết quả từ Dialog chọn Danh mục Con
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_SUB_CATEGORY, this, (requestKey, bundle) -> {
            int index = bundle.getInt(ListSelectionDialogFragment.RESULT_SELECTED_INDEX, -1);
            if (index != -1 && appConfig != null && selectedParentCategoryId != null) {
                CategoryConfig parent = appConfig.getCategories().stream()
                        .filter(c -> selectedParentCategoryId.equals(c.getId()))
                        .findFirst().orElse(null);

                if (parent != null) {
                    SubcategoryConfig selectedSub = parent.getSubcategories().get(index);
                    this.selectedSubCategoryId = selectedSub.getId(); // << GÁN GIÁ TRỊ CUỐI CÙNG

                    String displayText = parent.getName() + " > " + selectedSub.getName();
                    binding.fieldCategory.setText(displayText);
                    binding.fieldCategory.setError(null); // Xóa lỗi nếu có
                    binding.fieldCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));

                    updateSuggestedTags(this.selectedSubCategoryId);
                }
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
                // Lấy giá trị chuỗi từ danh sách tĩnh trong ViewModel
                selectedItemBehavior = AddItemViewModel.ITEM_BEHAVIORS.get(index);
                // Cập nhật giao diện
                binding.fieldItemBehavior.setText(selectedItemBehavior);
                binding.fieldItemBehavior.setError(null); // Xóa lỗi nếu có
                binding.fieldItemBehavior.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
            }
        });
    }

    private void updateSuggestedTags(String categoryId) {
        if (appConfig == null || categoryId == null) {
            return;
        }

        // Lấy danh sách tag từ AppConfig dựa vào categoryId
        List<String> suggestedTags = appConfig.getSuggestedTags().get(categoryId);

        // Xóa các chip cũ và danh sách tag đã chọn
        binding.chipGroupTags.removeAllViews();
        selectedTags.clear();

        if (suggestedTags == null || suggestedTags.isEmpty()) {
            // Nếu không có tag gợi ý, ẩn ChipGroup và hiện lại hướng dẫn
            binding.textViewTagsInstruction.setVisibility(View.VISIBLE);
            binding.chipGroupTags.setVisibility(View.GONE);
        } else {
            // Nếu có tag, hiện ChipGroup và ẩn hướng dẫn
            binding.textViewTagsInstruction.setVisibility(View.GONE);
            binding.chipGroupTags.setVisibility(View.VISIBLE);

            // Tạo các Chip từ danh sách gợi ý
            for (String tagName : suggestedTags) {
                Chip chip = createTagChip(tagName);
                binding.chipGroupTags.addView(chip);
            }
        }
    }

    private Chip createTagChip(String tagName) {
        Chip chip = new Chip(requireContext());
        chip.setText(tagName);
        chip.setCheckable(true); // << Cho phép chip được chọn/bỏ chọn
        chip.setCheckedIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.chip_background_color_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));

        // Xử lý khi người dùng chọn hoặc bỏ chọn một tag
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Giới hạn chỉ chọn tối đa 5 tags
                if (selectedTags.size() < 5) {
                    selectedTags.add(tagName);
                } else {
                    Toast.makeText(getContext(), "You can select up to 5 tags.", Toast.LENGTH_SHORT).show();
                    chip.setChecked(false); // Bỏ chọn nếu đã đủ 5
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
        itemToPost.setTags(new ArrayList<>(selectedTags));
        itemToPost.setTitle(binding.editTextTitle.getText().toString().trim());
        try {
            itemToPost.setPrice(Double.parseDouble(binding.editTextPrice.getText().toString().trim()));
        } catch (NumberFormatException e) {
            binding.tilPrice.setError("Giá không hợp lệ");
            return;
        }
        itemToPost.setDescription(binding.editTextDescription.getText().toString().trim());
        itemToPost.setCondition(selectedConditionId);

        itemToPost.setItemBehavior(selectedItemBehavior);
        itemToPost.setTags(new ArrayList<>(selectedTags));

        itemToPost.setCategory(selectedSubCategoryId);

        if (selectedGeoPoint != null && selectedAddress != null) {
            itemToPost.setLocation(selectedGeoPoint);
            itemToPost.setAddressString(selectedAddress);
        }
        itemToPost.setSearchKeywords(generateKeywords(itemToPost.getTitle()));

        viewModel.postItem(itemToPost, photoAdapter.getImageUris());
    }

    private List<String> generateKeywords(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return new ArrayList<>();
        }
        String cleanText = inputText.toLowerCase(Locale.getDefault()).replaceAll("[^a-z0-9\\s]", "");
        String[] words = cleanText.split("\\s+");
        HashSet<String> keywords = new HashSet<>(Arrays.asList(words));
        return new ArrayList<>(keywords);
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
        try {
            double price = Double.parseDouble(binding.editTextPrice.getText().toString().trim());
            if (price < 0) {
                binding.tilPrice.setError("Giá không được âm");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.tilPrice.setError("Giá không hợp lệ");
            return false;
        }
        if (selectedSubCategoryId == null) {
            binding.fieldCategory.setError("Vui lòng chọn danh mục chi tiết"); // Hiển thị lỗi trên field
            Toast.makeText(getContext(), "Vui lòng chọn danh mục chi tiết.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            binding.fieldCategory.setError(null);
        }
        if (selectedConditionId == null) {
            Toast.makeText(getContext(), "Vui lòng chọn tình trạng.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedGeoPoint == null || selectedAddress == null) {
            Toast.makeText(getContext(), "Vui lòng chọn vị trí.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedItemBehavior == null) {
            Toast.makeText(getContext(), "Vui lòng chọn phương thức giao dịch.", Toast.LENGTH_SHORT).show();
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
                            binding.textCurrentLocation.setText(address);
                            binding.textCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light_theme));
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
    public void onAddPhotoClick() {
        if (photoAdapter.getImageUris().size() >= 5) {
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
    public void onRemovePhotoClick(int position) {
        photoAdapter.removeImage(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}