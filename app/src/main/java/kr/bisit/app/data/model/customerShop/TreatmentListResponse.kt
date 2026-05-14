package kr.bisit.app.data.model.customerShop

data class TreatmentListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: TreatmentListData
)

data class TreatmentListData(
    val treatments: TreatmentPage
)

data class TreatmentPage(
    val content: List<TreatmentDetailItem>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

data class TreatmentDetailItem(
    val treatmentId: Long,
    val name: String,
    val description: String?,
    val price: Int,
    val durationMin: Int
)
