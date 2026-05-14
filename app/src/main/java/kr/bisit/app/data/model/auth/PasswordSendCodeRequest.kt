package kr.bisit.app.data.model.auth

import com.google.gson.annotations.SerializedName

data class PasswordSendCodeRequest(
    @SerializedName("loginId") val loginId: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("name") val name: String
)