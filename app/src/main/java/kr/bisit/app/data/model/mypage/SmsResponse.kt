package kr.bisit.app.data.model.mypage

data class SmsResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: SmsData?
)