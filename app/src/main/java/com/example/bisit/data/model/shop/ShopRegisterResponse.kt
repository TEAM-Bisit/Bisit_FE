package com.example.bisit.data.model.shop

data class ShopRegisterResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ShopRegisterData
)

data class ShopRegisterData(
    val shopId: Long,
    val businessRegNO: String,
    val name: String,
    val phone: String,
    val addressLine: String,
    val detailAddress: String,
    val latitude: Double,
    val longitude: Double
)
