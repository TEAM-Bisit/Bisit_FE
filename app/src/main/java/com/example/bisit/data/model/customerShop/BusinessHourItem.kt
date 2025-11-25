package com.example.bisit.data.model.customerShop

data class BusinessHourItem(
    val day: String,
    val openFrom: String?,
    val openTo: String?,
    val breakFrom: String?,
    val breakTo: String?,
    val isClosed: Boolean
)