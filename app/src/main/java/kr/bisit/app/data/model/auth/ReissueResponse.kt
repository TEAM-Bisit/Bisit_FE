package kr.bisit.app.data.model.auth

data class ReissueResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReissueData
)

data class ReissueData(
    val accessToken: String,
    val refreshToken: String
)