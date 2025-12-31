package com.example.bisit.data.model.member

data class MyProfileResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: MyProfileData
)

data class MyProfileData(
    val name: String,
    val email: String,
    val phone: String,
    val profileImage: String? = null
)
