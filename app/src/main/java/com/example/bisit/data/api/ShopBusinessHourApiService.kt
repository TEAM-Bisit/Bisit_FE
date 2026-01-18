package com.example.bisit.data.api

import com.example.bisit.data.model.shop.BaseResponse
import com.example.bisit.data.model.shop.BusinessHourResponse
import com.example.bisit.data.model.shop.UpdateBusinessHourRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ShopBusinessHourApiService {

    @GET("/api/shops/{shopId}/business-hours")
    suspend fun getShopBusinessHoursApi(
        @Path("shopId") shopId: Long
    ): BusinessHourResponse

    @PUT("/api/shops/{shopId}/business-hours")
    suspend fun updateBusinessHoursApi(
        @Path("shopId") shopId: Long,
        @Body request: UpdateBusinessHourRequest
    ): BaseResponse<String>
}
