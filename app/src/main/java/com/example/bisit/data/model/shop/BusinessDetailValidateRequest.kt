package com.example.bisit.data.model.shop

data class BusinessDetailValidateRequest(
    val businessRegNo: String,
    val representativeName: String,
    val openDate: String,
    val businessName: String
)