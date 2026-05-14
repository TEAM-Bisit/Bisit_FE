package kr.bisit.app.data.model.mypage

data class SmsVerifyResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: SmsVerifyData?
)