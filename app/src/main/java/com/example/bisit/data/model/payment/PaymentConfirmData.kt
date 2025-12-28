package com.example.bisit.data.model.payment

data class PaymentConfirmData(
    val paymentId: Long,
    val reservationId: Long,
    val orderId: String,
    val paymentKey: String,
    val paidAmount: Int,
    val status: String,
    val approvedAt: String
)
