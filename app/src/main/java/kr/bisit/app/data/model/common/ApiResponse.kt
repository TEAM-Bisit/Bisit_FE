package kr.bisit.app.data.model.common

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)
