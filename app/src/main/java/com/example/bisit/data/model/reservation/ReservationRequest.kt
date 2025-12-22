package com.example.bisit.data.model.reservation

data class ReservationRequest(
    val shopId: Long,
    val treatmentId: Long,
    val staffId: Long,
    val reservedDate: String,        // yyyy-MM-dd
    val startTime: String,           // HH:mm
    val serviceChannel: String,      // "SHOP" 또는 "VISIT"
    val customerName: String,
    val customerPhone: String,
    val visitAddressLine: String?,   // serviceChannel=VISIT일 때 필수
    val visitAddressDetail: String?,
    val termsAgreed: Boolean,
    val couponIssueId: Long?         // 쿠폰 ID (선택)
)
