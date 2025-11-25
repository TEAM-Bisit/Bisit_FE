package com.example.bisit.data.model.customerShop

data class CustomerShopResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: CustomerShopDetailItem?
)