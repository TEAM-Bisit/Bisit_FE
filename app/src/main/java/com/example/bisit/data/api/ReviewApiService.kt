package com.example.bisit.data.api

import com.example.bisit.data.model.review.ReviewRequest
import com.example.bisit.data.model.review.ReviewResponse
import com.example.bisit.data.model.review.ReviewUpdateResponse
import com.example.bisit.data.model.review.ReviewListResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ReviewApiService {
    @POST("/api/reviews")
    fun writeReview(@Body request: ReviewRequest): Call<ReviewResponse>

    @retrofit2.http.PUT("/api/reviews/{reviewId}")
    fun updateReview(
        @retrofit2.http.Path("reviewId") reviewId: Long,
        @Body request: ReviewRequest
    ): Call<ReviewUpdateResponse>

    @retrofit2.http.DELETE("/api/reviews/{reviewId}")
    fun deleteReview(
        @retrofit2.http.Path("reviewId") reviewId: Long
    ): Call<ReviewUpdateResponse>

    @retrofit2.http.GET("/api/reviews/shops/{shopId}")
    fun getShopReviews(
        @retrofit2.http.Path("shopId") shopId: Long,
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("size") size: Int
    ): Call<ReviewListResponse>

    @retrofit2.http.GET("/api/reviews/customers/my-reviews")
    fun getMyReviews(
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("size") size: Int
    ): Call<ReviewListResponse>
}
