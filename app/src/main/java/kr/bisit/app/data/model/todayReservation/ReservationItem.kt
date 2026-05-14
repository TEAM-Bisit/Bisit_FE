package kr.bisit.app.data.model.todayReservation

data class ReservationItem(
    val reservationId: Long,
    val status: String,
    val serviceStatus: String,
    val customerName: String,
    val treatmentName: String,
    val staffName: String,
    val visitAddressLine: String,
    val reservedDate: String,
    val startTime: String
)
