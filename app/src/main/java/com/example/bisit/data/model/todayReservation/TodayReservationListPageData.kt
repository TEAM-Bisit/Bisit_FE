package com.example.bisit.data.model.todayReservation

data class TodayReservationListPageData(
    val reservations: TodayReservationPageWrapper
)

data class TodayReservationPageWrapper(
    val content: List<ReservationListItem>,
    val totalPages: Int,
    val totalElements: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)
