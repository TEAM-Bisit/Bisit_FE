package com.example.bisit.data.model.todayReservation

data class ConfirmReservationRequest(
    val shopId: Long,
    val treatmentId: Long,
    val staffId: Long,
    val reservedDate: String,
    val startTime: String,
    val serviceChannel: String,
    val customerName: String,
    val customerPhone: String,
    val visitAddressLine: String?,
    val visitAddressDetail: String?,
    val termsAgreed: Boolean
)
