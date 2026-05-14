package kr.bisit.app.data.model.shop

data class BusinessValidateResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: BusinessValidateData
)

data class BusinessValidateData(
    val isValid: Boolean,
    val shopId: Long
)