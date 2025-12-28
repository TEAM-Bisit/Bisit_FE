package com.example.bisit.data.model.member

data class MemberUpdateResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: MemberUpdateData?
)

data class MemberUpdateData(
    val name: String,
    val email: String,
    val phone: String
)
