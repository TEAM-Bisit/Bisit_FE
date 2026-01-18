package com.example.bisit.data.repository.shop

import android.content.Context
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.BusinessHourItem
import com.example.bisit.data.model.shop.UpdateBusinessHourItem
import com.example.bisit.data.model.shop.UpdateBusinessHourRequest

class ShopBusinessHourRepository(
    context: Context
) {

    private val api = RetrofitClient.getShopBusinessHourApi(context)

    /* ===================== 조회 ===================== */

    suspend fun getBusinessHours(
        shopId: Long
    ): List<BusinessHourItem> {
        return api.getShopBusinessHoursApi(shopId).data
    }

    /* ===================== 수정 ===================== */

    suspend fun updateBusinessHours(
        shopId: Long,
        items: List<UpdateBusinessHourItem>
    ): String {
        val request = UpdateBusinessHourRequest(
            businessHours = items,
            validDays = true
        )
        return api.updateBusinessHoursApi(shopId, request).data
    }
}
