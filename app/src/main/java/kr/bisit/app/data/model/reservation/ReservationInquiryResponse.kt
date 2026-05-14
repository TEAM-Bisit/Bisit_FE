package kr.bisit.app.data.model.reservation

data class ReservationInquiryResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReservationInquiryData
)

data class ReservationInquiryData(
    val shopName: String,
    val staffName: String,
    val phoneNumber: String
)
