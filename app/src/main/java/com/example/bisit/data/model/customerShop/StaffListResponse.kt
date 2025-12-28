package com.example.bisit.data.model.customerShop

data class StaffListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<StaffData>
)
