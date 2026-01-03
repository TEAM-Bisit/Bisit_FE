package com.example.bisit.ui.customerMyReserve

data class MyReserveItem(
    val reservationId: String,
    val orderId: String?,
    val shopName: String,
    val status: String, // "예약", "완료", "취소"
    val treatmentName: String,
    val price: Int,
    val reservedDate: String,
    var isConfirmed: Boolean = false // 확정 여부 (로컬 상태 관리용)
)
