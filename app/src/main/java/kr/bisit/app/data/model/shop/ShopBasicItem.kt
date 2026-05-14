package kr.bisit.app.data.model.shop

data class ShopIntroduceResponse(
    val intro: String,
    val photos: List<ShopPhotoItem>,
    val serviceChannel: String
)

data class ShopPhotoItem(
    val id: Long,
    val url: String,
    val sortOrder: Int
)

data class ShopIntroduceRequest(
    val intro: String,
    val photoIds: List<Long>,
    val serviceChannel: String
)

data class ShopBasicInfoRequest(
    val name: String,
    val phone: String,
    val addressLine: String,
    val detailAddress: String
)
