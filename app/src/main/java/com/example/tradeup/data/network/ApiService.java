// File: src/main/java/com/example/tradeup/data/network/ApiService.java
package com.example.tradeup.data.network;

import com.example.tradeup.data.model.User;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface ApiService {

    @GET("users/{uid}")
    Call<User> getUserProfile(@Path("uid") String uid);

    @PATCH("users/{uid}")
    Call<Void> updateUserProfile(@Path("uid") String uid, @Body Map<String, Object> updates);
}