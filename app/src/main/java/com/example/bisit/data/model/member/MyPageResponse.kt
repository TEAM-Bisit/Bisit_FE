package com.example.bisit.data.model.member

data class MyPageResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: MyPageData
)

data class MyPageData(
    val memberId: Long,
    val name: String,
    val couponCount: Int,
    val reviewCount: Int
)
