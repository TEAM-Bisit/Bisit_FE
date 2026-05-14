package kr.bisit.app.data.model.shop

data class BaseResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)

data class TreatmentResponse(
    val treatmentId: Long,
    val name: String,
    val description: String,
    val price: Int,
    val durationHours: Int,
    val durationMinutes: Int,
    val photoUrl: String?,
    val isActive: Boolean
)

data class TreatmentListResponse(
    val treatments: TreatmentPage
)

data class TreatmentPage(
    val content: List<TreatmentResponse>,
    val totalPages: Int,
    val totalElements: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)
