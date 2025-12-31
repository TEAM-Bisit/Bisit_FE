package com.example.bisit.data.api

import com.example.bisit.data.model.shop.BusinessValidateRequest
import com.example.bisit.data.model.shop.BusinessValidateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopApiService {
    @POST("/api/shops/regist/validate-business")
    fun validateBusiness(
        @Body request: BusinessValidateRequest
    ): Call<BusinessValidateResponse>
}