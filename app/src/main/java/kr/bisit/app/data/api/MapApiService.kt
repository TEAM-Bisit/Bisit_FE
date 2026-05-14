package kr.bisit.app.data.api

import kr.bisit.app.data.model.map.ShopListResponse
import kr.bisit.app.data.model.map.ShopSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MapApiService {
    
    @GET("/api/shops")
    suspend fun getShopsInBounds(
        @Query("minLatitude") minLatitude: Double,
        @Query("maxLatitude") maxLatitude: Double,
        @Query("minLongitude") minLongitude: Double,
        @Query("maxLongitude") maxLongitude: Double,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20,
        @Query("homeServiceOnly") homeServiceOnly: Boolean? = null
    ): Response<ShopListResponse>

    @GET("/api/shops/map/search")
    suspend fun searchShopsInBounds(
        @Query("minLatitude") minLatitude: Double,
        @Query("maxLatitude") maxLatitude: Double,
        @Query("minLongitude") minLongitude: Double,
        @Query("maxLongitude") maxLongitude: Double,
        @Query("categories") categories: List<String>? = null,
        @Query("keyword") keyword: String? = null,
        @Query("homeServiceOnly") homeServiceOnly: Boolean? = null,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20
    ): Response<ShopSearchResponse>

    @GET("/api/shops/search")
    suspend fun searchShopsByName(
        @Query("name") name: String,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20
    ): Response<ShopSearchResponse>
}
