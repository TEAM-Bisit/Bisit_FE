package com.example.bisit.data.model.shop

data class ShopIndustryResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: IndustryData?
)

data class IndustryData(
    val category: String
)