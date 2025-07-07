// File: src/main/java/com/example/tradeup/data/repository/LocationRepositoryImpl.java
package com.example.tradeup.data.repository;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.tradeup.core.utils.Callback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener; // << QUAN TRỌNG: Import thêm

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class LocationRepositoryImpl implements LocationRepository {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context appContext;
    private LocationCallback locationCallback;

    @Inject
    public LocationRepositoryImpl(@ApplicationContext Context context) {
        this.appContext = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public void getCurrentLocation(Callback<Location> callback) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onFailure(new SecurityException("Location permission not granted."));
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            callback.onSuccess(location);
                        } else {
                            requestNewLocationData(callback);
                        }
                    }
                })
                .addOnFailureListener(e -> requestNewLocationData(callback));
    }

    private void requestNewLocationData(Callback<Location> callback) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(8000)
                .build();

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                fusedLocationClient.removeLocationUpdates(this); // Dừng cập nhật ngay

                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    callback.onSuccess(lastLocation);
                } else {
                    callback.onFailure(new Exception("Failed to get location from result."));
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
}