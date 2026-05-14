package kr.bisit.app.data.model.category

data class CategoryShopResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: CategoryShopData
)