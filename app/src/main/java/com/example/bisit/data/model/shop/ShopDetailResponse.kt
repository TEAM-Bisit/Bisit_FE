package com.example.bisit.data.model.shop

data class ShopDetailResponse(
    val shopName: String,
    val category: String,
    val reviewCount: Int,
    val averageRating: Double,
    val shortIntro: String,
    val address: String,
    val detailAddress: String,
    val todayBusinessHours: String,
    val weeklyBusinessHours: List<WeeklyBusinessHour>,
    val latestNotice: ShopNotice?,
    val photos: List<String>,
    val channel: String,
    val phone: String
)

data class WeeklyBusinessHour(
    val day: String,
    val openFrom: String?,
    val openTo: String?,
    val breakFrom: String?,
    val breakTo: String?,
    val isClosed: Boolean
)

data class ShopNotice(
    val title: String,
    val createdAt: String // 필요 시 LocalDateTime 변환
)
