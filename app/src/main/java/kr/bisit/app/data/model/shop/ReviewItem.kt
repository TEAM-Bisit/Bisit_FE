package kr.bisit.app.data.model.shop

data class ReviewItem(
    val author: String,
    val content: String,
    val date: String,
    val serviceName: String? = null,
    val staffName: String? = null,
    val rating: Int = 5
)