package com.example.bisit.data.model.todayReservation

data class TodayReservationStatusData(
    val status: String,
    val serviceStatus: String,
    val reservationId: Long,
    val paymentStatus: String,
    val treatmentName: String,
    val reservedDate: String,
    val startTime: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val staffName: String,
    val price: Int
)