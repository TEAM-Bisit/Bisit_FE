package com.example.bisit.data.model.payment

data class PaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Int
)
