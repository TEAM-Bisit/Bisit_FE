package com.example.bisit.data.model.auth

import com.google.gson.annotations.SerializedName

data class PasswordVerifyResponse(
    @SerializedName("token") val token: String
)