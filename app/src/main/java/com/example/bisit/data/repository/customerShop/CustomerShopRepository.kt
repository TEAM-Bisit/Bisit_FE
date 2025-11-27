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
}