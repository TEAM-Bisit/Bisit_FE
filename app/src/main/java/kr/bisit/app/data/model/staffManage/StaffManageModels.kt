package kr.bisit.app.data.model.staffManage

enum class StaffStatus {
    PENDING, APPROVED, REJECTED, DELETED
}

data class AddStaffRequest(
    val name: String,
    val email: String,
    val phone: String
)

// 직원-가게 관계 응답
data class StaffResponse(
    val id: Long,
    val memberId: Long,
    val shopId: Long,
    val status: StaffStatus
)

// 직원 신청 목록 아이템
data class PendingStaffItem(
    val staffId: Long,
    val name: String,
    val phone: String
)

// 승인된 직원 목록 아이템
data class ApprovedStaffItem(
    val staffId: Long,
    val name: String
)

// 공통 API 응답
data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)
