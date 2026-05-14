package kr.bisit.app.ui.shop

/**
 * Shop 화면에서 사용하는
 * 직원 신청 상태 모델
 */
data class StaffRequestState(
    val hasPendingRequest: Boolean = false,
    val isLoading: Boolean = false
)
