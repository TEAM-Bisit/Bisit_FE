package kr.bisit.app.data.model.customerShop

data class CustomerShopResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: CustomerShopDetailItem?
)