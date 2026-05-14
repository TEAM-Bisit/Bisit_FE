package kr.bisit.app.data.model.reservation

data class ReservationResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReservationData?
)
