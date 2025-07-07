package com.example.tradeup.data.repository;

import android.location.Location;

import com.example.tradeup.core.utils.Callback;

public interface LocationRepository {
    void getCurrentLocation(Callback<Location> callback);
}
