package kr.bisit.app.data.model.auth

import com.google.gson.annotations.SerializedName

data class PasswordResetRequest(
    @SerializedName("loginId") val loginId: String,
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)