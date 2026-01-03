package com.example.bisit.data.model.reservation

data class ReservationDetailResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReservationDetailData
)

data class ReservationDetailData(
    val reservedDate: String,
    val startTime: String,
    val durationMin: Int,
    val price: Int,
    val customerName: String,
    val status: String,
    val reservationId: Long,
    val orderId: String,
    val shopName: String,
    val shopAddress: String,
    val canConfirm: Boolean,
    val cancellationReason: String?,
    val cancellationReason: String?,
    val canceledAt: String?,
    val isReviewed: Boolean = false
)
