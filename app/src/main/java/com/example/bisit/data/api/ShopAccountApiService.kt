package com.example.bisit.data.api

import com.example.bisit.data.model.common.ApiResponse
import com.example.bisit.data.model.shop.*
import retrofit2.http.*

interface ShopAccountApiService {

    @GET("/api/shops/{shopId}/account")
    suspend fun getShopAccount(
        @Path("shopId") shopId: Long
    ): ApiResponse<ShopAccountResponse>

    @POST("/api/shops/{shopId}/account")
    suspend fun registerOrUpdateAccount(
        @Path("shopId") shopId: Long,
        @Body request: ShopAccountRequest
    ): ApiResponse<ShopAccountResponse>
}
