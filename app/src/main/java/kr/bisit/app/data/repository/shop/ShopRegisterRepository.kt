package kr.bisit.app.data.repository.shop

import android.content.Context
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.shop.ShopRegisterRequest
import kr.bisit.app.data.model.shop.ShopRegisterResponse

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
