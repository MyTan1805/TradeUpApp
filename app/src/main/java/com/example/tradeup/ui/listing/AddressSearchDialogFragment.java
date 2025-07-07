// File: src/main/java/com/example/tradeup/ui/listing/AddressSearchDialogFragment.java
package com.example.tradeup.ui.listing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tradeup.R;
import com.example.tradeup.data.model.nominatim.GeocodingResult;
import com.example.tradeup.data.network.NominatimApiService;
import com.example.tradeup.databinding.DialogAddressSearchBinding;
import com.example.tradeup.ui.adapters.AddressResultAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AddressSearchDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "AddressSearchDialog";
    public static final String REQUEST_KEY = "address_request";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    private DialogAddressSearchBinding binding;
    private AddressResultAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Inject
    NominatimApiService nominatimApiService;

    public static AddressSearchDialogFragment newInstance() {
        return new AddressSearchDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_TradeUp_BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddressSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchInput();
        showInitialMessage();
    }

    private void setupRecyclerView() {
        adapter = new AddressResultAdapter(address -> {
            // Khi người dùng nhấn vào một địa chỉ
            Bundle result = new Bundle();
            result.putString(KEY_ADDRESS, address.displayName);
            result.putDouble(KEY_LATITUDE, Double.parseDouble(address.lat));
            result.putDouble(KEY_LONGITUDE, Double.parseDouble(address.lon));
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });
        binding.recyclerViewResults.setAdapter(adapter);
    }

    private void setupSearchInput() {
        binding.editTextSearchAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                handler.removeCallbacks(searchRunnable); // Hủy bỏ tìm kiếm cũ
                if (query.length() < 3) {
                    showInitialMessage();
                    return;
                }
                // Tạo một tìm kiếm mới với độ trễ (debounce)
                searchRunnable = () -> performSearch(query);
                handler.postDelayed(searchRunnable, 500); // 500ms delay
            }
        });
    }

    private void performSearch(String query) {
        showLoading();
        nominatimApiService.search(query, "json", 1).enqueue(new Callback<List<GeocodingResult>>() {
            @Override
            public void onResponse(@NonNull Call<List<GeocodingResult>> call, @NonNull Response<List<GeocodingResult>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    showResults(response.body());
                } else {
                    showEmptyMessage("No results found for '" + query + "'");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GeocodingResult>> call, @NonNull Throwable t) {
                showEmptyMessage("Search failed: " + t.getMessage());
            }
        });
    }

    private void showInitialMessage() {
        binding.progressBarSearch.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmptyOrError.setVisibility(View.VISIBLE);
        binding.textViewEmptyOrError.setText("Start typing to search for an address");
    }

    private void showLoading() {
        binding.progressBarSearch.setVisibility(View.VISIBLE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmptyOrError.setVisibility(View.GONE);
    }

    private void showResults(List<GeocodingResult> results) {
        binding.progressBarSearch.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.VISIBLE);
        binding.textViewEmptyOrError.setVisibility(View.GONE);
        adapter.submitList(results);
    }

    private void showEmptyMessage(String message) {
        binding.progressBarSearch.setVisibility(View.GONE);
        binding.recyclerViewResults.setVisibility(View.GONE);
        binding.textViewEmptyOrError.setVisibility(View.VISIBLE);
        binding.textViewEmptyOrError.setText(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}