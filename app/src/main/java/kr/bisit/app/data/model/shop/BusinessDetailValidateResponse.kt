package kr.bisit.app.data.model.shop

data class BusinessDetailValidateResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: Boolean
)