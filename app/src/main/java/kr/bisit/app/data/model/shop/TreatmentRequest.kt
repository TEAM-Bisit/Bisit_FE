package kr.bisit.app.data.model.shop

data class TreatmentRequest(
    val name: String,
    val description: String,
    val price: Int,
    val durationHours: Int,
    val durationMinutes: Int,
    val isActive: Boolean
)
