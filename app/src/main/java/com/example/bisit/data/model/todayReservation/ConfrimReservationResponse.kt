package com.example.bisit.data.model.todayReservation

data class ConfirmReservationData(
    val shopId: Long,
    val shopName: String,
    val shopPhone: String,
    val shopAddress: String,
    val treatmentId: Long,
    val treatmentName: String,
    val treatmentPrice: Int,
    val durationMin: Int,
    val staffId: Long,
    val staffName: String,
    val reservedDate: String,
    val startTime: String,
    val endTime: String,
    val serviceChannel: String,
    val visitAddress: String?,
    val customerName: String,
    val customerPhone: String
)
