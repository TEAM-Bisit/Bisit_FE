package kr.bisit.app.data.model.review

data class ReviewListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReviewListData
)

data class ReviewListData(
    val reviews: ReviewPage
)

data class ReviewPage(
    val content: List<ReviewDetailItem>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

data class ReviewDetailItem(
    val reviewId: Long, // Added based on requirement for Edit/Delete
    val serviceName: String?,
    val staffName: String?,
    val rating: Int,
    val content: String,
    val visitDate: String?,
    val reviewerName: String?,
    val createdAt: String?
)
