package com.example.bisit.data.model.payment

data class PaymentFailResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: PaymentFailData?
)

data class PaymentFailData(
    val reservationId: Long,
    val orderId: String,
    val message: String,
    val couponRestored: Boolean
)
