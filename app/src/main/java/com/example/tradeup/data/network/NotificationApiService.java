package com.example.tradeup.data.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationApiService {
    @POST("send-notification")
    Call<NotificationResponse> sendNotification(@Body NotificationRequest request);
}