package kr.bisit.app.data.model.reservation

data class StaffAvailabilityResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: StaffAvailabilityData?
)
