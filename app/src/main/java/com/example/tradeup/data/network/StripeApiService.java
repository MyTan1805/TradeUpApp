package com.example.tradeup.data.network;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StripeApiService {

    @POST("create-payment-intent")
    Call<Map<String, String>> createPaymentIntent(@Body Map<String, Object> params);

    @POST("capture-payment-intent")
    Call<Map<String, Object>> capturePaymentIntent(@Body Map<String, String> params);
}