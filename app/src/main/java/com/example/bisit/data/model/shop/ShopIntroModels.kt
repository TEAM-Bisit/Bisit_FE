package com.example.bisit.data.model.shop

data class ShopPhotoResponse(
    val success: Boolean,
    val data: PhotoData
)
data class PhotoData(
    val photoId: Long,
    val url: String
)