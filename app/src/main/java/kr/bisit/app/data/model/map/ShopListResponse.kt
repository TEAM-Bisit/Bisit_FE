package kr.bisit.app.data.model.map

data class ShopListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ShopListData
)

data class ShopListData(
    val content: List<ShopMapItem>,
    val nextCursor: Long?,
    val hasNext: Boolean,
    val size: Int
)
