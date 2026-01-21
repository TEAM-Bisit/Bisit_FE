package com.example.bisit.data.model.payment

data class PaymentFailRequest(
    val orderId: String,
    val failReason: String
)
