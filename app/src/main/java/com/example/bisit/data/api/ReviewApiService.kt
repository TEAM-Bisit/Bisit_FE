package com.example.bisit.data.api

import com.example.bisit.data.model.review.ReviewRequest
import com.example.bisit.data.model.review.ReviewResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ReviewApiService {
    @POST("/api/reviews")
    fun writeReview(@Body request: ReviewRequest): Call<ReviewResponse>
}
