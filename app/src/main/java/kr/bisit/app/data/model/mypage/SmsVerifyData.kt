package kr.bisit.app.data.model.mypage

data class SmsVerifyData(
    val verified: Boolean,
    val message: String?
)