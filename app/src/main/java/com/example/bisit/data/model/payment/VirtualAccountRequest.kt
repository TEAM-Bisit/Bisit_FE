package com.example.bisit.data.model.payment

data class VirtualAccountRequest(
    val orderId: String,
    val amount: Long,
    val bank: String,
    val customerName: String
)
