package com.example.bisit.data.model.payment

data class VirtualAccountRequest(
    val orderId: String,
    val amount: Int,
    val bank: String,
    val customerName: String
)
