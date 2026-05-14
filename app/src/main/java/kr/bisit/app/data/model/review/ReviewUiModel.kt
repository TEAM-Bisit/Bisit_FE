package kr.bisit.app.data.model.review

data class ReviewUiModel(
    val reviewId: Long,
    val reservationId: String,
    val content: String,
    val score: Int
)
