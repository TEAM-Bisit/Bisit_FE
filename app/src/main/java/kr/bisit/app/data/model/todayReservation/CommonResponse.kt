package kr.bisit.app.data.model.todayReservation

data class CommonResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)

