package kr.bisit.app.data.api

import kr.bisit.app.data.model.shop.BusinessDetailValidateRequest
import kr.bisit.app.data.model.shop.BusinessDetailValidateResponse
import kr.bisit.app.data.model.shop.BusinessValidateRequest
import kr.bisit.app.data.model.shop.BusinessValidateResponse
import kr.bisit.app.data.model.shop.ShopHolidayRequest
import kr.bisit.app.data.model.shop.ShopHolidayResponse
import kr.bisit.app.data.model.shop.ShopIndustryRequest
import kr.bisit.app.data.model.shop.ShopIndustryResponse
import kr.bisit.app.data.model.shop.ShopIntroduceRequest
import kr.bisit.app.data.model.shop.ShopIntroduceResponse
import kr.bisit.app.data.model.shop.ShopOperatingHoursRequest
import kr.bisit.app.data.model.shop.ShopOperatingHoursResponse
import kr.bisit.app.data.model.shop.ShopPhotoResponse
import kr.bisit.app.data.model.shop.ShopRegisterRequest
import kr.bisit.app.data.model.shop.ShopRegisterResponse
import kr.bisit.app.data.model.shop.ShopSetHolidayRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
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
        @Path("shopId") shopId: Long,
        @Part file: MultipartBody.Part
    ): Call<ShopPhotoResponse>

    @POST("/api/shops/regist/{shopId}/introduce")
    fun updateIntroduce(
        @Path("shopId") shopId: Long,
        @Body request: ShopIntroduceRequest
    ): Call<ShopIntroduceResponse>

    @POST("/api/shops/regist/{shopId}/industry")
    fun updateIndustry(
        @Path("shopId") shopId: Long,
        @Body request: ShopIndustryRequest
    ): Call<ShopIndustryResponse>

    @POST("/api/shops/regist/{shopId}/holiday")
    fun updateHolidaySettings(
        @Path("shopId") shopId: Long,
        @Body request: ShopHolidayRequest
    ): Call<ShopHolidayResponse>

    @PUT("/api/shops/regist/{shopId}/setholiday")
    fun setHolidayDays(
        @Path("shopId") shopId: Long,
        @Body request: ShopSetHolidayRequest
    ): Call<ShopOperatingHoursResponse>

    @POST("/api/shops/regist/{shopId}/hours")
    fun updateOperatingHours(
        @Path("shopId") shopId: Long,
        @Body request: ShopOperatingHoursRequest
    ): Call<ShopOperatingHoursResponse>
}