package kr.bisit.app.data.model.map

data class GeocodingResponse(
    val status: String,
    val addresses: List<Address>
)