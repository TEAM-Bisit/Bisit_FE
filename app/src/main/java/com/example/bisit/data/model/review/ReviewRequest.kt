package com.example.bisit.data.model.review

data class ReviewRequest(
    val reservationId: String,
    val rating: Int,
    val content: String
)