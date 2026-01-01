package com.example.bisit.data.model.shop

data class ShopRegisterRequest(
    val businessRegNO: String,
    val name: String,
    val phone: String,
    val addressLine: String,
    val detailAddress: String
)