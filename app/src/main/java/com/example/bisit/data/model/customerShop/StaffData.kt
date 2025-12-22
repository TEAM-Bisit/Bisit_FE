package com.example.bisit.data.model.customerShop

data class StaffData(
    val staffId: Long,
    val staffName: String,
    val description: String?,
    val image: String?,
    val averageRating: Double,
    val reviewCount: Int
)
