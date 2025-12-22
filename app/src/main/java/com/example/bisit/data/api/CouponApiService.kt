package com.example.bisit.data.api

import com.example.bisit.data.model.coupon.ApplicableCouponResponse
import com.example.bisit.data.model.coupon.CouponListResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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
}
