package com.example.bisit.data.repository.shop

import android.content.Context
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.ShopRegisterRequest
import com.example.bisit.data.model.shop.ShopRegisterResponse

class ShopRegisterRepository(
    private val context: Context
) {

    private val api = RetrofitClient.getShopRegisterApi(context)

    suspend fun registerShop(
        request: ShopRegisterRequest
    ): ShopRegisterResponse {
        return api.registerShop(request)
    }
}
