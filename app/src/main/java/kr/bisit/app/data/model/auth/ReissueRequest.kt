package kr.bisit.app.data.model.auth

data class ReissueRequest(
    val refreshToken: String,
    val authProvider: String
)