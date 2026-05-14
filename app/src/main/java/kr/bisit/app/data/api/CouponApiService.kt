package kr.bisit.app.data.api

import kr.bisit.app.data.model.coupon.*
import retrofit2.Call
import retrofit2.http.*

interface CouponApiService {
    @GET("/api/coupons/members/{memberId}")
    fun getMyCoupons(
        @Path("memberId") memberId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<CouponListResponse>

    @GET("/api/coupons/members/{memberId}/applicable")
    fun getApplicableCoupons(
        @Path("memberId") memberId: Long,
        @Query("treatmentPrice") treatmentPrice: Int
    ): Call<ApplicableCouponResponse>

    // Owner Coupon Management
    @GET("/api/coupons/shops/{shopId}")
    fun getShopCoupons(
        @Path("shopId") shopId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<OwnerCouponResponse>

    @POST("/api/coupons/shops/{shopId}")
    fun createShopCoupon(
        @Path("shopId") shopId: Long,
        @Body request: CreateCouponRequest
    ): Call<SingleCouponResponse>

    @PUT("/api/coupons/{couponId}/shops/{shopId}")
    fun updateShopCoupon(
        @Path("couponId") couponId: Long,
        @Path("shopId") shopId: Long,
        @Body request: UpdateCouponRequest
    ): Call<SingleCouponResponse>

    @DELETE("/api/coupons/{couponId}/shops/{shopId}")
    fun deleteShopCoupon(
        @Path("couponId") couponId: Long,
        @Path("shopId") shopId: Long
    ): Call<kr.bisit.app.data.model.auth.AuthResponse> // Using AuthResponse for simple success/fail
}
