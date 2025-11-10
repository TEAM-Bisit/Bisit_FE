package com.example.bisit.ui.shop.model

data class Review(
    val id: Long,
    val date: String,
    val service: String,
    val user: String,
    val rating: Int,
    val content: String
)


data class ServiceItem(
    val id: Long,
    var title: String,
    var desc: String,
    var price: Int,
    var imageUrl: String? = null,
    var durationMin: Int = 0
)


data class Notice(
    val id: Long,
    var title: String,
    var content: String,
    var date: String
)