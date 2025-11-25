package com.example.bisit.data.api

import com.example.bisit.data.model.customerShop.CustomerShopResponse
import com.example.bisit.data.model.customerShop.CustomerShopIntroduceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomerShopApiService {

    @GET("api/shops/{shopId}")
    suspend fun getShopDetail(
        @Path("shopId") shopId: Long
    ): Response<CustomerShopResponse>

    @GET("api/shops/{shopId}/basic/introduce")
    suspend fun getShopIntroduce(
        @Path("shopId") shopId: Long
    ): Response<CustomerShopIntroduceResponse>
}