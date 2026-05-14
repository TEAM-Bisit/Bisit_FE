package kr.bisit.app.data.model.map

import com.google.gson.annotations.SerializedName

data class NaverSearchResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NaverSearchItem>
)

data class NaverSearchItem(
    val title: String, // 가게/건물 이름
    val link: String?,
    val category: String,
    val description: String?,
    val telephone: String?,
    val address: String?,
    val roadAddress: String?,
    @SerializedName("mapx") val mapx: String, // KATECH 좌표 x
    @SerializedName("mapy") val mapy: String  // KATECH 좌표 y
)
