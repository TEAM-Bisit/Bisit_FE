package kr.bisit.app.data.model.category

data class CategoryShopData(
    val content: List<CategoryShopItem>,
    val nextCursor: Long?,
    val hasNext: Boolean,
    val size: Int
)