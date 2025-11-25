package com.example.bisit.data.model.customerShop

data class CustomerShopIntroduceResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: CustomerShopIntroduceData?
)
