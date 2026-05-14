package kr.bisit.app.data.model.shop

data class ReviewResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReviewData
)

data class ReviewData(
    val reviews: ReviewPage
)
