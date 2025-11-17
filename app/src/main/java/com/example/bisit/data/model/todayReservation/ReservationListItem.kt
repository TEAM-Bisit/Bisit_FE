package com.example.bisit.data.model.todayReservation

data class ReservationListItem(
    val status: String,
    val reservationId: Long,
    val shopName: String,
    val reservedDate: String,
    val treatmentName: String,
    val price: Int
)