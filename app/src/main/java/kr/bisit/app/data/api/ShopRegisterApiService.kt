package kr.bisit.app.data.api

import kr.bisit.app.data.model.shop.ShopRegisterRequest
import kr.bisit.app.data.model.shop.ShopRegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopRegisterApiService {

    @POST("/api/shops/regist")
    suspend fun registerShop(
        @Body request: ShopRegisterRequest
    ): ShopRegisterResponse
}
