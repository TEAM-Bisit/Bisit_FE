package kr.bisit.app.data.model.store

data class StoreItem(
    val name: String,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val isOpen: Boolean,
    val businessHours: String,
    val tags: List<String>,
    val images: List<Int>,
    val hasVisitService: Boolean
)