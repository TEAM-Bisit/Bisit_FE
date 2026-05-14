package kr.bisit.app.data.model.staffManage

data class StaffEnrollResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: StaffEnrollData
)

data class StaffEnrollData(
    val id: Long,
    val memberId: Long,
    val shopId: Long,
    val status: String
)