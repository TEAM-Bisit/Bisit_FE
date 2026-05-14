package kr.bisit.app.data.api

import kr.bisit.app.data.model.map.GeocodingResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverGeocodingApiService {
    @GET("map-geocode/v2/geocode")
    fun geocode(
        @Query("query") query: String
    ): Call<GeocodingResponse>
}