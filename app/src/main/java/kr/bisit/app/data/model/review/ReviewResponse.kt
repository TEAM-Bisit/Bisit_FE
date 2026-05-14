package kr.bisit.app.data.model.review

data class ReviewResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReviewData?
)