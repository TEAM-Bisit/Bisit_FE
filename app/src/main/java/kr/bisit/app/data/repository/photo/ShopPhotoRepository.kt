package kr.bisit.app.data.repository.photo

import android.content.Context
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.common.ApiResponse
import kr.bisit.app.data.model.photo.ShopPhotoUploadResponse
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
