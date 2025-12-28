package com.example.bisit.data.model.member

data class MemberUpdateRequest(
    val name: String,
    val email: String,
    val phone: String,
    val verificationCode: String?
)
