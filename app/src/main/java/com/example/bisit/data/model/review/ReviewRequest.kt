package com.example.bisit.data.model.review

data class ReviewRequest(
    val reservationId: Long,
    val rating: Int,
    val content: String
)