package com.example.bisit.data.model.category

data class CategoryShopData(
    val content: List<CategoryShopItem>,
    val nextCursor: Long?,
    val hasNext: Boolean,
    val size: Int
)