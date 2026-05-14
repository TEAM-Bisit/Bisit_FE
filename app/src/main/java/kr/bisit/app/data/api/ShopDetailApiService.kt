package kr.bisit.app.data.api

import kr.bisit.app.data.model.common.ApiResponse
import kr.bisit.app.data.model.shop.ShopDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ShopDetailApiService {

    @GET("/api/shops/{shopId}")
    suspend fun getShopDetail(
        @Path("shopId") shopId: Long
    ): ApiResponse<ShopDetailResponse>
}
