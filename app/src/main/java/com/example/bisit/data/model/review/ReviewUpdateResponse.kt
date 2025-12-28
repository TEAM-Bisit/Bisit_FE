package com.example.bisit.data.model.review

data class ReviewUpdateResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String?
)
