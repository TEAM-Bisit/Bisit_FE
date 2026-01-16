package com.example.bisit.data.model.member

data class MemberRoleResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: MemberRoleData
)

data class MemberRoleData(
    val memberId: Int,
    val name: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String
)