package kr.bisit.app.data.model.todayReservation

data class ChangeStatusRequest(
    val targetStatus: String,
    val cancellationReason: String?
)