package com.example.bisit.data.model.shop

data class ShopAccountResponse(
    val id: Long,
    val bankCode: String,
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val isVerified: Boolean,
    val verifiedAt: String?
)

data class ShopAccountRequest(
    val bankCode: String,
    val accountNumber: String,
    val accountHolder: String
)
