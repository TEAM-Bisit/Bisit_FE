package kr.bisit.app.data.model.customerShop

data class StaffListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<StaffData>
)
