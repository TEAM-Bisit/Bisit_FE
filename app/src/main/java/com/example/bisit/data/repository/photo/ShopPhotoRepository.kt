package com.example.bisit.data.repository.photo

import android.content.Context
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.common.ApiResponse
import com.example.bisit.data.model.photo.ShopPhotoUploadResponse
import okhttp3.MultipartBody

class ShopPhotoRepository(private val context: Context) {

    private val shopPhotoApi =
        RetrofitClient.getShopPhotoApi(context)

    // 사진 추가
    suspend fun uploadShopPhoto(
        shopId: Long,
        file: MultipartBody.Part
    ): ApiResponse<ShopPhotoUploadResponse> {
        return shopPhotoApi.uploadShopPhoto(shopId, file)
    }

    // 사진 삭제
    suspend fun deleteShopPhoto(
        shopId: Long,
        photoId: Long
    ): ApiResponse<String> {
        return shopPhotoApi.deleteShopPhoto(shopId, photoId)
    }
}
