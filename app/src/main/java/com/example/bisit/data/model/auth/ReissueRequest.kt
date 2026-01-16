package com.example.bisit.data.model.auth

data class ReissueRequest(
    val refreshToken: String,
    val authProvider: String
)