package com.example.bisit.ui.customerMyReserve

data class MyReserveItem(
    val reservationId: String,
    val shopName: String,
    val status: String // "예약", "완료", "취소"
)
