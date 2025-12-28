package com.example.bisit.data.model.reservation

data class ReservationData(
    val reservationId: Long,
    val orderId: String,
    val shopId: Long,
    val shopName: String,
    val treatmentId: Long,
    val treatmentName: String,
    val staffId: Long,
    val staffName: String,
    val reservedDate: String,
    val startTime: String,
    val endTime: String,
    val listedPrice: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val status: String
)
