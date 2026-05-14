package kr.bisit.app.data.model.shop

data class BaseDeleteResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String
)
