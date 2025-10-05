package com.example.naottae.data.model.map

data class GeocodingResponse(
    val status: String,
    val addresses: List<Address>
)