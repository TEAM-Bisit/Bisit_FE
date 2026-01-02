package com.example.bisit.data.model.reservList

/* ---------- 공통 응답 ---------- */

data class ReservationListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReservationListData
)

data class ReservationListData(
    val reservations: ReservationPage
)

data class ReservationPage(
    val content: List<ReservationListItem>,
    val totalPages: Int,
    val totalElements: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

/* ---------- 목록 아이템 ---------- */

data class ReservationListItem(
    val reservationId: Long,
    val status: String,
    val serviceStatus: String,
    val customerName: String,
    val treatmentName: String,
    val staffName: String,
    val reservedDate: String,
    val startTime: String
)

/* ---------- 상세 ---------- */

data class ReservationDetailResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReservationDetailData
)

data class ReservationDetailData(
    val status: String,
    val serviceStatus: String,
    val reservationId: Long,
    val reservationPaymentStatus: String,
    val treatmentName: String,
    val reservedDate: String,
    val startTime: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val staffName: String,
    val price: Int
)

/* ---------- 상태 enum (선택) ---------- */

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELED_BY_CUSTOMER,
    CANCELED_BY_SHOP,
    COMPLETED,
    CUSTOMER_CONFIRMED,
    NO_SHOW
}
