package kr.bisit.app.data.api

import kr.bisit.app.data.model.common.ApiResponse
import kr.bisit.app.data.model.photo.ShopPhotoUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface ShopPhotoApiService {

    // 사진 추가
    @Multipart
    @POST("/api/shops/{shopId}/photos")
    suspend fun uploadShopPhoto(
        @Path("shopId") shopId: Long,
        @Part file: MultipartBody.Part
    ): ApiResponse<ShopPhotoUploadResponse>

    // 사진 삭제
    @DELETE("/api/shops/{shopId}/photos/{photoId}")
    suspend fun deleteShopPhoto(
        @Path("shopId") shopId: Long,
        @Path("photoId") photoId: Long
    ): ApiResponse<String>
}
