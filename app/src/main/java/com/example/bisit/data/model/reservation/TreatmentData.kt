package com.example.bisit.data.model.reservation

data class TreatmentData(
    val treatmentId: Long,
    val treatmentName: String,
    val durationMin: Int,
    val price: Int,
    val photoUrl: String?,
    val availableTimes: List<String>
)
