package com.example.bisit.data.model.customerShop

data class BusinessHourResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<BusinessHourItem>
)
