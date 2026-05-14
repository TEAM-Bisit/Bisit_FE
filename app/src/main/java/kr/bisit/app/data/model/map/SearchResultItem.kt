package kr.bisit.app.data.model.map

sealed class SearchResultItem {
    data class InternalShop(val shop: ShopMapItem) : SearchResultItem()
    data class ExternalPlace(val place: NaverSearchItem) : SearchResultItem()

    val name: String
        get() = when (this) {
            is InternalShop -> shop.shopName
            is ExternalPlace -> place.title.replace("<b>", "").replace("</b>", "")
        }

    val itemCategory: String
        get() = when (this) {
            is InternalShop -> shop.category
            is ExternalPlace -> place.category
        }
}
