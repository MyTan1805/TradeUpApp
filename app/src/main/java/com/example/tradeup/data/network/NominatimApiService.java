// File: data/network/NominatimApiService.java
package com.example.tradeup.data.network;

import com.example.tradeup.data.model.nominatim.GeocodingResult;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimApiService {
    // format=json&addressdetails=1
    @GET("search")
    Call<List<GeocodingResult>> search(
            @Query("q") String query,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails
    );
}