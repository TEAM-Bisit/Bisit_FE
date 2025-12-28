package com.example.bisit.data.model.auth

data class LoginResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: TokenData?
)

data class TokenData(
    val accessToken: String,
    val refreshToken: String
)