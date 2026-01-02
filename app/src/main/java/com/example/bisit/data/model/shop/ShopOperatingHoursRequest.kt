package com.example.bisit.data.model.shop

data class ShopOperatingHoursRequest(
    val openFrom: String,
    val openTo: String,
    val breakFrom: String?,
    val breakTo: String?
)