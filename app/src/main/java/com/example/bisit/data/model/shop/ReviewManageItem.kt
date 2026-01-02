package com.example.bisit.data.model.shop

import java.time.LocalDate
import java.time.LocalDateTime

data class ReviewManageItem(
    val serviceName: String,
    val staffName: String,
    val rating: Int,
    val content: String,
    val visitDate: LocalDate,
    val reviewerName: String,
    val createdAt: LocalDateTime
)
