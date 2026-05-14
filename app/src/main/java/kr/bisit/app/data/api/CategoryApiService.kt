package kr.bisit.app.data.api

import kr.bisit.app.data.model.category.CategoryShopResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CategoryApiService {
    @GET("/api/shops/category/{category}")
    suspend fun getShopsByCategory(
        @Path("category") category: String,
        @Query("userLatitude") lat: Double?,
        @Query("userLongitude") lng: Double?,
        @Query("sortType") sortType: String = "DISTANCE",
        @Query("cursor") cursor: Long = 0,
        @Query("size") size: Int = 20
    ): CategoryShopResponse

    @GET("/api/shops/{shopId}/business-hours")
    suspend fun getBusinessHours(
        @Path("shopId") shopId: Long
    ): retrofit2.Response<kr.bisit.app.data.model.customerShop.BusinessHourResponse>
}