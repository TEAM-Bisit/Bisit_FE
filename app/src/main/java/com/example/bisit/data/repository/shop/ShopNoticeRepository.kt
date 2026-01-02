package com.example.bisit.data.repository.shop

import android.content.Context
import com.example.bisit.data.api.ShopNoticeApiService
import com.example.bisit.data.model.shop.*
import com.example.bisit.data.api.RetrofitClient

class ShopNoticeRepository(context: Context) {

    private val api: ShopNoticeApiService =
        RetrofitClient.getShopNoticeApi(context)

    suspend fun getNotices(
        shopId: Long,
        sortOrder: String = "desc"
    ): ApiResponse<ShopNoticeListResponse>? {
        val response = api.getShopNotices(shopId, sortOrder)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createNotice(
        shopId: Long,
        title: String,
        content: String
    ): ApiResponse<ShopNoticeResponse>? {
        val response = api.createShopNotice(
            shopId,
            ShopNoticeRequest(title, content)
        )
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateNotice(
        shopId: Long,
        noticeId: Long,
        title: String,
        content: String
    ): ApiResponse<ShopNoticeResponse>? {
        val response = api.updateShopNotice(
            shopId,
            noticeId,
            ShopNoticeRequest(title, content)
        )
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteNotice(
        shopId: Long,
        noticeId: Long
    ): ApiResponse<String>? {
        val response = api.deleteShopNotice(shopId, noticeId)
        return if (response.isSuccessful) response.body() else null
    }
}
