package com.example.bisit.data.model.customerShop

data class CustomerShopData(
    val shopName: String,
    val category: String,
    val reviewCount: Int,
    val averageRating: Double,
    val shortIntro: String,
    val address: String,
    val detailAddress: String,
    val todayBusinessHours: String,
    val weeklyBusinessHours: List<BusinessHourItem>,
    val latestNotice: NoticeItem?,
    val photos: List<String>,
    val channel: String,
    val phone: String
)