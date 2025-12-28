package com.example.bisit.data.repository.customerShop

import android.content.Context
import com.example.bisit.data.api.CustomerShopApiService
import com.example.bisit.data.model.customerShop.*
import com.example.bisit.data.api.RetrofitClient
import retrofit2.Response

class CustomerShopRepository(context: Context) {
    private val api = RetrofitClient.getCustomerShopApi(context.applicationContext)

    suspend fun getShopDetail(context: Context, shopId: Long): Response<CustomerShopResponse> {
        return api.getShopDetail(shopId)
    }

    suspend fun getShopIntroduce(context: Context, shopId: Long): Response<CustomerShopIntroduceResponse> {
        return api.getShopIntroduce(shopId)
    }

    suspend fun getShopTreatments(shopId: Long, page: Int, size: Int): Response<TreatmentListResponse> {
        return api.getShopTreatments(shopId, page, size)
    }

    fun getShopReviews(context: Context, shopId: Long, page: Int, size: Int): retrofit2.Call<com.example.bisit.data.model.review.ReviewListResponse> {
        // Review API is in ReviewApiService which is Call-based currently.
        // We will expose it as is or wrap it if we wanted to use suspend but let's stick to what we have in VM or change VM.
        // Wait, the plan said "Add getShopReviews ... I will try to use Coroutines for consistency".
        // But ReviewApiService uses Call. So I can't easily make it suspend without changing ReviewApiService or wrapping it using await().
        // Let's just expose the Call or better, let's grab ReviewApi here and wrap it.
        return RetrofitClient.getReviewApi(context.applicationContext).getShopReviews(shopId, page, size)
    }
}