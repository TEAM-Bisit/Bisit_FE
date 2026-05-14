package kr.bisit.app.data.model.auth

data class LoginRequest(
    val loginId: String,
    val password: String
)