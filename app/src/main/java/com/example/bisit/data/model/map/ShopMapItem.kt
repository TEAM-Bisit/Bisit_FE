package com.example.bisit.data.model.map

data class ShopMapItem(
    val shopId: Long,
    val shopName: String,
    val photos: List<String>,
    val category: String,
    val averageRating: Double,
    val reviewCount: Int,
    val businessHours: String,
    val treatments: List<String>,
    val channel: String,
    val latitude: Double,
    val longitude: Double
)
