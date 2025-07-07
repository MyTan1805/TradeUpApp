// File: data/model/nominatim/GeocodingResult.java
package com.example.tradeup.data.model.nominatim;

import com.google.gson.annotations.SerializedName;

public class GeocodingResult {
    @SerializedName("place_id")
    public long placeId;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("lat")
    public String lat;

    @SerializedName("lon")
    public String lon;
}