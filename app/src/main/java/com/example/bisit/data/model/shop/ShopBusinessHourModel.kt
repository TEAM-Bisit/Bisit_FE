package com.example.bisit.data.model.shop

/* ===================== GET 응답 ===================== */

data class BusinessHourResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<BusinessHourItem>
)

data class BusinessHourItem(
    val day: String,          // MONDAY ~ SUNDAY
    val openFrom: String?,
    val openTo: String?,
    val breakFrom: String?,
    val breakTo: String?,
    val isClosed: Boolean
)

/* ===================== PUT 요청 ===================== */

data class UpdateBusinessHourRequest(
    val businessHours: List<UpdateBusinessHourItem>,
    val validDays: Boolean
)

data class UpdateBusinessHourItem(
    val day: String,
    val openFrom: String?,
    val openTo: String?,
    val breakFrom: String?,
    val breakTo: String?,
    val validOpenHours: Boolean,
    val validBreakTimeComplete: Boolean,
    val validBreakHours: Boolean,
    val breakTimeWithinOpenHours: Boolean
)

