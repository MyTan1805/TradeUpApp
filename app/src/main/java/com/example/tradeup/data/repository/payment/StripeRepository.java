package com.example.tradeup.data.repository.payment;

import androidx.annotation.NonNull;
import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.network.StripeApiService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class StripeRepository {

    private final StripeApiService stripeApiService;

    @Inject
    public StripeRepository(StripeApiService stripeApiService) {
        this.stripeApiService = stripeApiService;
    }

    public void createPaymentIntent(double amount, @NonNull String currency, @NonNull String customerName, @NonNull Callback<Map<String, String>> callback) {
        Map<String, Object> params = new HashMap<>();
        // Stripe tính toán bằng đơn vị nhỏ nhất (cents)
        params.put("amount", (long) (amount * 100)); // << SỬA LẠI: Nhân 100 cho USD
        params.put("currency", currency);
        params.put("customerName", customerName);

        stripeApiService.createPaymentIntent(params).enqueue(new retrofit2.Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onFailure(new IOException("API Error: " + response.code() + " - " + errorBody));
                    } catch (IOException e) {
                        callback.onFailure(new IOException("API Error: " + response.code()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }
}