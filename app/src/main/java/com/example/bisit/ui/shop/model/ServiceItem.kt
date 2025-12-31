package com.example.bisit.ui.shop.model

import android.net.Uri

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
    var imageUri: Uri? = null,
    var durationMin: Int = 0
)

data class Notice(
    val id: Long,
    var title: String,
    var content: String,
    var date: String
)