package kr.bisit.app.data.model.customerShop

data class CustomerShopIntroduceData(
    val intro: String?,
    val photos: List<CustomerShopPhotoItem>?,
    val serviceChannel: String?
)
