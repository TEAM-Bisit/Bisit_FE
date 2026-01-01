package com.example.bisit.data.api

import com.example.bisit.data.model.shop.BusinessDetailValidateRequest
import com.example.bisit.data.model.shop.BusinessDetailValidateResponse
import com.example.bisit.data.model.shop.BusinessValidateRequest
import com.example.bisit.data.model.shop.BusinessValidateResponse
import com.example.bisit.data.model.shop.ShopIndustryRequest
import com.example.bisit.data.model.shop.ShopIndustryResponse
import com.example.bisit.data.model.shop.ShopIntroduceRequest
import com.example.bisit.data.model.shop.ShopIntroduceResponse
import com.example.bisit.data.model.shop.ShopPhotoResponse
import com.example.bisit.data.model.shop.ShopRegisterRequest
import com.example.bisit.data.model.shop.ShopRegisterResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ShopApiService {
    @POST("/api/shops/regist/validate-business")
    fun validateBusiness(
        @Body request: BusinessValidateRequest
    ): Call<BusinessValidateResponse>

    @POST("/api/shops/regist/validate-detail")
    fun validateDetail(
        @Body request: BusinessDetailValidateRequest
    ): Call<BusinessDetailValidateResponse>

    @POST("/api/shops/regist")
    fun registerShop(
        @Body request: ShopRegisterRequest
    ): Call<ShopRegisterResponse>

    @Multipart
    @POST("/api/shops/regist/{shopId}/photos")
    fun uploadPhoto(
        @Path("shopId") shopId: Int,
        @Part file: MultipartBody.Part
    ): Call<ShopPhotoResponse>

    @POST("/api/shops/regist/{shopId}/introduce")
    fun updateIntroduce(
        @Path("shopId") shopId: Int,
        @Body request: ShopIntroduceRequest
    ): Call<ShopIntroduceResponse>

    @POST("/api/shops/regist/{shopId}/industry")
    fun updateIndustry(
        @Path("shopId") shopId: Int,
        @Body request: ShopIndustryRequest
    ): Call<ShopIndustryResponse>
}