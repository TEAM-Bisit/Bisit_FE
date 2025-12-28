package com.example.bisit.data.model.reservation

data class ReservationListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReservationPageData?
)

data class ReservationPageData(
    val reservations: ReservationPageContent
)

data class ReservationPageContent(
    val content: List<ReservationItem>,
    val totalPages: Int,
    val totalElements: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

data class ReservationItem(
    val status: String,
    val reservationId: Long,
    val orderId: String?,
    val shopName: String,
    val reservedDate: String,
    val treatmentName: String,
    val price: Int
)
