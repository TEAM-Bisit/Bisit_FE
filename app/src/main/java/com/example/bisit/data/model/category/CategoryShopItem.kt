package com.example.bisit.data.model.category

data class CategoryShopItem(
    val shopId: Long,
    val shopName: String,
    val photos: List<String>,
    val category: String,
    val averageRating: Double,
    val reviewCount: Int,
    val businessHours: String?,
    val treatments: List<String>,
    val channel: String,
    val hasVisitService: Boolean?
)