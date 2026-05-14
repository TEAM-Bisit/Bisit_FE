package kr.bisit.app.data.model.photo

data class ShopPhotoUploadResponse(
    val photoId: Long,
    val url: String,
    val sortOrder: Int
)