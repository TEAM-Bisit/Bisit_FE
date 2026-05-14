package kr.bisit.app.data.repository.shop

import android.content.Context
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.common.ApiResponse
import kr.bisit.app.data.model.shop.*

class ShopRepository(private val context: Context) {

    private val shopBasicApi =
        RetrofitClient.getShopBasicApi(context)

    private val shopDetailApi =
        RetrofitClient.getShopDetailApi(context)

    private val shopAccountApi =
        RetrofitClient.getShopAccountApi(context)

    //매장 기본 정보 (Basic)
    suspend fun getShopIntroduce(
        shopId: Long
    ): ApiResponse<ShopIntroduceResponse> {
        return shopBasicApi.getShopIntroduce(shopId)
    }

    suspend fun updateShopIntroduce(
        shopId: Long,
        request: ShopIntroduceRequest
    ): ApiResponse<String> {
        return shopBasicApi.updateShopIntroduce(shopId, request)
    }

    suspend fun updateShopBasicInfo(
        shopId: Long,
        request: ShopBasicInfoRequest
    ): ApiResponse<String> {
        return shopBasicApi.updateShopBasicInfo(shopId, request)
    }

    // 샵 상세 조회
    suspend fun getShopDetail(
        shopId: Long
    ): ApiResponse<ShopDetailResponse> {
        return shopDetailApi.getShopDetail(shopId)
    }

    // 계좌
    suspend fun getShopAccount(
        shopId: Long
    ): ApiResponse<ShopAccountResponse> {
        return shopAccountApi.getShopAccount(shopId)
    }

    suspend fun registerOrUpdateAccount(
        shopId: Long,
        request: ShopAccountRequest
    ): ApiResponse<ShopAccountResponse> {
        return shopAccountApi.registerOrUpdateAccount(shopId, request)
    }
}
