package kr.bisit.app.data.model.member

data class MemberUpdateRequest(
    val name: String,
    val email: String,
    val phone: String,
    val verificationCode: String?
)
