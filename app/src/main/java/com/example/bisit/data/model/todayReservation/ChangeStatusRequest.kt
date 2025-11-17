package com.example.bisit.data.model.todayReservation

data class ChangeStatusRequest(
    val targetStatus: String,
    val cancellationReason: String?
)