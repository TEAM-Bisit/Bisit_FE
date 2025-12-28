package com.example.bisit.data.model.reservation

data class StaffAvailabilityData(
    val staffId: Long,
    val staffName: String,
    val date: String,
    val availableTimes: List<String>,
    val treatments: List<TreatmentData>
)
