package com.example.naeottae.data.model.map

data class GeocodingResponse(
    val status: String,
    val addresses: List<Address>
)