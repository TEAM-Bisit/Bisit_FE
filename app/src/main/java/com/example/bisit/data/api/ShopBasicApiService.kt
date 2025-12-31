package com.example.bisit.data.api

import com.example.bisit.data.model.common.ApiResponse
import com.example.bisit.data.model.shop.*
import retrofit2.http.*

interface ShopBasicApiService {

    // 매장 소개 조회
    @GET("/api/shops/{shopId}/basic/introduce")
    suspend fun getShopIntroduce(
        @Path("shopId") shopId: Long
    ): ApiResponse<ShopIntroduceResponse>

    // 매장 소개 수정
    @PUT("/api/shops/{shopId}/basic/introduce")
    suspend fun updateShopIntroduce(
        @Path("shopId") shopId: Long,
        @Body request: ShopIntroduceRequest
    ): ApiResponse<String>

    // 매장 기본 정보 수정
    @PUT("/api/shops/{shopId}/basic/info")
    suspend fun updateShopBasicInfo(
        @Path("shopId") shopId: Long,
        @Body request: ShopBasicInfoRequest
    ): ApiResponse<String>
}
