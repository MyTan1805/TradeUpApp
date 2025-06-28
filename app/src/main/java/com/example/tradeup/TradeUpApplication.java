package com.example.tradeup;

import android.app.Application;
import com.google.android.libraries.places.api.Places;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class TradeUpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!Places.isInitialized()) {
            String apiKey = getString(R.string.maps_api_key); // Lấy key từ resource
            if (apiKey.equals("DEFAULT_API_KEY_IF_NOT_FOUND") || apiKey.isEmpty()){
            } else {
                Places.initialize(getApplicationContext(), apiKey);
            }
        }
    }
}