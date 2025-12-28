package com.example.bisit.data.api

import com.example.bisit.data.model.customerShop.CustomerShopResponse
import com.example.bisit.data.model.customerShop.CustomerShopIntroduceResponse
import com.example.bisit.data.model.customerShop.StaffListResponse
import com.example.bisit.data.model.customerShop.TreatmentListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomerShopApiService {

    @GET("/api/shops/{shopId}")
    suspend fun getShopDetail(
        @Path("shopId") shopId: Long
    ): Response<CustomerShopResponse>

    @GET("/api/shops/{shopId}/basic/introduce")
    suspend fun getShopIntroduce(
        @Path("shopId") shopId: Long
    ): Response<CustomerShopIntroduceResponse>

    @GET("/api/shops/{shopId}/staff/list")
    suspend fun getStaffList(
        @Path("shopId") shopId: Long
    ): Response<StaffListResponse>

    @GET("/api/treatments/shops/{shopId}")
    suspend fun getShopTreatments(
        @Path("shopId") shopId: Long,
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("size") size: Int
    ): Response<TreatmentListResponse>
}