package com.example.naeottae.data.api

import com.example.naeottae.data.model.map.GeocodingResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverGeocodingApiService {
    @GET("map-geocode/v2/geocode")
    fun geocode(
        @Query("query") query: String
    ): Call<GeocodingResponse>
}