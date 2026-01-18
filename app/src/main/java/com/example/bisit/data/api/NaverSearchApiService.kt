package com.example.bisit.data.api

import com.example.bisit.data.model.map.NaverSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverSearchApiService {
    @GET("v1/search/local.json")
    suspend fun searchLocal(
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random",
        @Query("coordinate") coordinate: String = "wgs84"
    ): Response<NaverSearchResponse>
}
