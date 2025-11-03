package com.example.bisit.data.model.shop

data class ShopDetailItem(
    val name: String,
    val category: String,
    val review: String,
    val rating: String,
    val summary: String,
    val address: String,
    val openInfo: String,
    val phone: String,
    val notice: String,
    val noticeTime: String,
    val weeklyOpenHours: List<String>,
    var isExpanded: Boolean = false
)