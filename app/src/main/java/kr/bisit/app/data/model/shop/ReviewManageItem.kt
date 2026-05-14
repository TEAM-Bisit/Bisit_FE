package kr.bisit.app.data.model.shop

data class ReviewManageItem(
    val reviewId: Long,
    val serviceName: String,
    val staffName: String,
    val rating: Int,
    val content: String,
    val visitDate: String,
    val reviewerName: String,
    val createdAt: String
)
