package com.example.bisit.data.model.todayReservation

data class TodayReservationData(
    val tab: String,
    val sortBy: String,
    val totalCount: Int,
    val reservations: List<ReservationItem>
)