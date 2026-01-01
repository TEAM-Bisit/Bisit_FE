package com.example.bisit.data.model.shop

data class ShopPhotoResponse(
    val success: Boolean,
    val data: PhotoData
)
data class PhotoData(
    val photoId: Int,
    val url: String
)

data class ShopIntroduceRequest(
    val intro: String,
    val photoIds: List<Int>,
    val serviceChannel: String // "SHOP" 또는 "VISIT"
)

data class ShopIntroduceResponse(
    val success: Boolean,
    val message: String
)