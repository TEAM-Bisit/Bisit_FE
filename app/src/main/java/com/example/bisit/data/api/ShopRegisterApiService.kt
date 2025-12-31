package com.example.bisit.data.api

import com.example.bisit.data.model.shop.ShopRegisterRequest
import com.example.bisit.data.model.shop.ShopRegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopRegisterApiService {

    @POST("/api/shops/regist")
    suspend fun registerShop(
        @Body request: ShopRegisterRequest
    ): ShopRegisterResponse
}
