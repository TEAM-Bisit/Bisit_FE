package com.example.bisit.data.model.review

data class ReviewRequest(
    val reservationId: String,
    val score: Int,
    val content: String
)