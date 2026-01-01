package com.example.bisit.data.api

import com.example.bisit.data.model.shop.BusinessDetailValidateRequest
import com.example.bisit.data.model.shop.BusinessDetailValidateResponse
import com.example.bisit.data.model.shop.BusinessValidateRequest
import com.example.bisit.data.model.shop.BusinessValidateResponse
import com.example.bisit.data.model.shop.ShopRegisterRequest
import com.example.bisit.data.model.shop.ShopRegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopApiService {
    @POST("/api/shops/regist/validate-business")
    fun validateBusiness(
        @Body request: BusinessValidateRequest
    ): Call<BusinessValidateResponse>

    @POST("/api/shops/regist/validate-detail")
    fun validateDetail(
        @Body request: BusinessDetailValidateRequest
    ): Call<BusinessDetailValidateResponse>

    @POST("/api/shops/regist")
    fun registerShop(
        @Body request: ShopRegisterRequest
    ): Call<ShopRegisterResponse>
}