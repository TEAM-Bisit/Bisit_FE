package kr.bisit.app.data.model.shop

data class ReviewPage(
    val content: List<ReviewManageItem>,
    val totalPages: Int,
    val totalElements: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)
