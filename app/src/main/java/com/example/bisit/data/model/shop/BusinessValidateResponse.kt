package com.example.bisit.data.model.shop

data class BusinessValidateResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: Boolean
)