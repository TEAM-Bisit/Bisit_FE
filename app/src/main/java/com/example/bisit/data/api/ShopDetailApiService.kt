package com.example.bisit.data.api

import com.example.bisit.data.model.common.ApiResponse
import com.example.bisit.data.model.shop.ShopDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ShopDetailApiService {

    @GET("/api/shops/{shopId}")
    suspend fun getShopDetail(
        @Path("shopId") shopId: Long
    ): ApiResponse<ShopDetailResponse>
}
