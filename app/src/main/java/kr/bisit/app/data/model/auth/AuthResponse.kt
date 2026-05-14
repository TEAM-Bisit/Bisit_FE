package kr.bisit.app.data.model.auth

data class AuthResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String?
)
