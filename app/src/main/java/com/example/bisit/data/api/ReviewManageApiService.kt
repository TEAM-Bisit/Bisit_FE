package com.example.bisit.data.api

import com.example.bisit.data.model.shop.*
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewManageApi {

    /** 매장 관리자용 리뷰 목록 조회 */
    @GET("/api/reviews/shops/{shopId}/owner")
    suspend fun getShopReviews(
        @Path("shopId") shopId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ReviewResponse

    /** 매장 관리자용 리뷰 삭제 */
    @DELETE("/api/reviews/{reviewId}/shops/{shopId}/owner")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Long,
        @Path("shopId") shopId: Long
    ): BaseDeleteResponse
}
