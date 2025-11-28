package com.example.bisit.data.model.customerShop

data class CustomerShopUiItem(
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
    val intro: String? = null,
    val photos: List<String>? = null,
    var isExpanded: Boolean = false
)
