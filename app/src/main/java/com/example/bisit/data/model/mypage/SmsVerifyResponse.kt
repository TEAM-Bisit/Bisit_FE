package com.example.bisit.data.model.mypage

data class SmsVerifyResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: SmsVerifyData?
)