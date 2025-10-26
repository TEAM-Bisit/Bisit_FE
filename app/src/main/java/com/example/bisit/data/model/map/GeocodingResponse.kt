package com.example.bisit.data.model.map

data class GeocodingResponse(
    val status: String,
    val addresses: List<Address>
)