package com.example.bisit.data.model.category

data class CategoryShopResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: CategoryShopData
)