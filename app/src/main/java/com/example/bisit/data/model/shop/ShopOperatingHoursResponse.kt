package com.example.bisit.data.model.shop

data class ShopOperatingHoursResponse(
    val success: Boolean,
    val data: List<OperatingDayData>?
)

data class OperatingDayData(
    val day: String,
    val openFrom: String?,
    val openTo: String?,
    val breakFrom: String?,
    val breakTo: String?,
    val isClosed: Boolean
)