package kr.bisit.app.data.model.auth

import com.google.gson.annotations.SerializedName

data class PasswordVerifyCodeRequest(
    @SerializedName("loginId") val loginId: String,
    @SerializedName("code") val code: String
)